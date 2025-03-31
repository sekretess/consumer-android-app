package io.sekretess.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.KyberPreKeyRecords;
import io.sekretess.dto.RegistrationAndDeviceId;
import io.sekretess.repository.DbHelper;
import io.sekretess.repository.SekretessSignalProtocolStore;
import io.sekretess.ui.LoginActivity;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.KeycloakManager;

import net.openid.appauth.AuthState;

import org.signal.libsignal.protocol.DuplicateMessageException;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.InvalidVersionException;
import org.signal.libsignal.protocol.LegacyMessageException;
import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.SessionCipher;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.UntrustedIdentityException;
import org.signal.libsignal.protocol.ecc.Curve;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.ecc.ECPrivateKey;
import org.signal.libsignal.protocol.groups.GroupCipher;
import org.signal.libsignal.protocol.groups.GroupSessionBuilder;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.message.SenderKeyDistributionMessage;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.signal.libsignal.protocol.util.Medium;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SignalProtocolService extends SekretessBackgroundService {
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static SekretessSignalProtocolStore signalProtocolStore;
    private int deviceId;
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private DbHelper dbHelper;
    private static final Map<String, GroupCipher> groupCipherTable = new ConcurrentHashMap<>();

    public static final int SIGNAL_PROTOCOL_NOTIFICATION = 2;
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);

    private final BroadcastReceiver encryptedMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String encryptedMessage = intent.getStringExtra("encryptedMessage");
            String exchangeName = intent.getStringExtra("exchangeName");
            String messageType = intent.getStringExtra("messageType");
            String sender = intent.getStringExtra("sender");
            decryptMessage(encryptedMessage, sender, exchangeName, messageType);
        }
    };

    private final BroadcastReceiver loginEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                SharedPreferences globalVariables = getApplicationContext()
                        .getSharedPreferences("global-variables", MODE_PRIVATE);
                globalVariables
                        .edit()
                        .putString("username", intent.getStringExtra("queueName"))
                        .apply();
                if (signalProtocolStore == null) {
                    Log.w("SignalProtocolService", "Signal protocol store is null. Initializing protocolStore...");
                    IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
                    if (groupCipherTable.isEmpty()) {
                        Log.w("SignalProtocolService", "Group chat chipper (Advertisement) is empty. " +
                                "Initializing re-joining to channels...");
                        ApiClient.refreshChannelSubscription(dbHelper.getAuthState().getIdToken());
                    }
                    if (identityKeyPair == null) {
                        Log.w("SignalProtocolService", "No cryptographic keys found. Initializing keys...");
                        KeyMaterial keyMaterial = initializeKeys();
                        if (KeycloakManager.getInstance().updateKeys(dbHelper.getAuthState(), keyMaterial)) {
                            Toast.makeText(getApplicationContext(), "One time keys updated", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "One time keys update failed", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w("SignalProtocolService", "Cryptographic keys found. Loading from database...");
                        RegistrationAndDeviceId registrationId = dbHelper.getRegistrationId();
                        signalProtocolStore = new SekretessSignalProtocolStore(context, identityKeyPair, registrationId.getRegistrationId());
                        SignedPreKeyRecord signedPreKeyRecord = dbHelper.getSignedPreKeyRecord();
                        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
                        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
                        deviceId = registrationId.getDeviceId();

                        dbHelper.loadPreKeyRecords(signalProtocolStore);
                        dbHelper.loadSessions(signalProtocolStore);
                        dbHelper.loadKyberPreKeys(signalProtocolStore);
                    }
                } else {
                    for (SignedPreKeyRecord signedPreKeyRecord : signalProtocolStore.loadSignedPreKeys()) {
                        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());

                    }
                }
            } catch (Exception e) {
                Log.e("SignalProtocolService", "Something wrong gone during handle login event. No cryptographic env initialized!", e);

            }
        }
    };

    private final BroadcastReceiver initializeKeyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SignalProtocolService", "Initialize event received");
            String email = intent.getStringExtra("email");
            String username = intent.getStringExtra("username");
            String password = intent.getStringExtra("password");


            try {
                KeyMaterial keyMaterial = initializeKeys();

                if (KeycloakManager.getInstance()
                        .createUser(username, email, password, keyMaterial)) {
                    startLoginActivity();
                } else {
                    broadcastSignupFailed();
                }
            } catch (Exception e) {
                Log.e("SignalProtocolService", "Error occurred during initialize user", e);
                broadcastSignupFailed();
            }
        }
    };

    private void startLoginActivity() {
        Intent loginActivityIntent = new Intent(SignalProtocolService.this, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginActivityIntent);
    }

    private final BroadcastReceiver updateOpkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SignalProtocolService", "Update event received");
            try {
                KeyMaterial keyMaterial = updateOneTimeKeys();
                AuthState authState = dbHelper.getAuthState();
                if (authState != null) {
                    if (KeycloakManager.getInstance().updateKeys(authState, keyMaterial)) {
                        Toast.makeText(getApplicationContext(), "One time keys updated", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "One time keys update failed", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("SignalProtocolService", "Error occurred during update OPK", e);
            }
        }
    };


    @Override
    public void started(Intent intent) {
        serviceInstances.getAndSet(1);
        getApplicationContext().registerReceiver(encryptedMessageBroadcastReceiver, new IntentFilter(Constants.EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE), RECEIVER_EXPORTED);
        getApplicationContext().registerReceiver(initializeKeyBroadcastReceiver, new IntentFilter(Constants.EVENT_INITIALIZE_KEY), RECEIVER_EXPORTED);
        getApplicationContext().registerReceiver(updateOpkBroadcastReceiver, new IntentFilter(Constants.EVENT_UPDATE_KEY), RECEIVER_EXPORTED);
        getApplicationContext().registerReceiver(loginEventBroadcastReceiver, new IntentFilter(Constants.EVENT_LOGIN), RECEIVER_EXPORTED);

        Log.i("SignalProtocolService", "All broadcast receivers registered");
        Log.i("SignalProtocolService", "signalProtocolStore = " + signalProtocolStore);
        if (signalProtocolStore == null) {
            Log.i("SignalProtocolService", "SignalProtocolStore is null. Starting logging in process");
            startLoginActivity();
        }
        dbHelper = DbHelper.getInstance(this);
    }

    @Override
    public String getChannelId() {
        return "sekretess:signal-protocol-service-channel";
    }

    @Override
    public int getNotificationId() {
        return SIGNAL_PROTOCOL_NOTIFICATION;
    }

    @Override
    public void destroyed() {
        serviceInstances.getAndSet(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public KeyMaterial updateOneTimeKeys() throws InvalidKeyException {
        //Generate one-time prekeys
        PreKeyRecord[] opk = generatePreKeys(15);
        SignedPreKeyRecord signedPreKeyRecord = dbHelper.getSignedPreKeyRecord();
        KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(dbHelper.getIdentityKeyPair().getPrivateKey());
        return new KeyMaterial(dbHelper.getRegistrationId().getRegistrationId(),
                serializeSignedPreKeys(opk), signedPreKeyRecord,
                dbHelper.getIdentityKeyPair(), dbHelper.getSignedPreKeyRecord().getSignature(),
                serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords()),
                base64Encoder.encodeToString(kyberPreKeyRecords.getLastResortKyberPreKeyRecord()
                        .getKeyPair().getPublicKey().serialize()),
                kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId());
    }

    private KeyMaterial initializeKeys() throws InvalidKeyException {

        ECKeyPair ecKeyPair = Curve.generateKeyPair();
        IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
        IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

        int registrationId = KeyHelper.generateRegistrationId(false);
        signalProtocolStore = new SekretessSignalProtocolStore(getApplicationContext(),
                identityKeyPair, registrationId);
        ECKeyPair keyPair = Curve.generateKeyPair();
        byte[] signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(),
                keyPair.getPublicKey().serialize());
        ;
        //Generate one-time prekeys
        PreKeyRecord[] opk = generatePreKeys(15);
        this.deviceId = Math.abs(new Random().nextInt(Medium.MAX_VALUE - 1));
        Log.i("SignalProtocolService", "Keys initialized");
        dbHelper.storeIdentityKeyPair(identityKeyPair);
        dbHelper.storeRegistrationId(registrationId, deviceId);

        SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(keyPair, signature);
        KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());

        return new KeyMaterial(registrationId, serializeSignedPreKeys(opk), signedPreKeyRecord,
                identityKeyPair, signature, serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords()),
                base64Encoder.encodeToString(kyberPreKeyRecords.getLastResortKyberPreKeyRecord()
                        .getKeyPair().getPublicKey().serialize()),
                kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId());
    }

    private KyberPreKeyRecords generateKyberPreKeys(ECPrivateKey ecPrivateKey) {
        // Generate post quantum resistance keys
        int count = 15;
        KyberPreKeyRecord[] kyberPreKeyRecords = new KyberPreKeyRecord[count];

        for (int i = 0; i < count; i++) {
            KyberPreKeyRecord kyberPreKeyRecord = generateKyberPreKey(ecPrivateKey);
            kyberPreKeyRecords[i] = kyberPreKeyRecord;
        }
        KyberPreKeyRecord lastResortKyberPreKeyRecord = generateKyberPreKey(ecPrivateKey);
        // Generated post quantum keys
        return new KyberPreKeyRecords(lastResortKyberPreKeyRecord, kyberPreKeyRecords);
    }

    private KyberPreKeyRecord generateKyberPreKey(ECPrivateKey ecPrivateKey) {
        int kyberSignedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        KEMKeyPair kemKeyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        KyberPreKeyRecord kyberPreKeyRecord = new KyberPreKeyRecord(kyberSignedPreKeyId,
                System.currentTimeMillis(), kemKeyPair, ecPrivateKey.calculateSignature(kemKeyPair.getPublicKey().serialize()));
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);
        dbHelper.storeKyberPreKey(kyberPreKeyRecord);
        return kyberPreKeyRecord;
    }


    private SignedPreKeyRecord generateSignedPreKey(ECKeyPair keyPair, byte[] signature) {
        //Generate signed prekeyRecord
        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyId,
                System.currentTimeMillis(), keyPair, signature);
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

        dbHelper.storeSignedPreKeyRecord(signedPreKeyRecord);
        return signedPreKeyRecord;
    }

    private PreKeyRecord[] generatePreKeys(int count) {
        PreKeyRecord[] preKeyRecords = new PreKeyRecord[count];
        SecureRandom preKeyRecordIdGenerator = new SecureRandom();
        for (int i = 0; i < count; i++) {
            int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
            ECKeyPair ecKeyPair = Curve.generateKeyPair();
            PreKeyRecord preKeyRecord = new PreKeyRecord(id, ecKeyPair);
            signalProtocolStore.storePreKey(id, preKeyRecord);
            preKeyRecords[i] = preKeyRecord;
        }
        dbHelper.storePreKeyRecords(preKeyRecords);
        return preKeyRecords;
    }

    private String[] serializeSignedPreKeys(PreKeyRecord[] preKeyRecords) throws InvalidKeyException {
        String[] serializedOneTimePreKeys = new String[preKeyRecords.length];

        int idx = 0;
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            serializedOneTimePreKeys[idx++] = preKeyRecord.getId() + ":" + base64Encoder
                    .encodeToString(preKeyRecord.getKeyPair().getPublicKey().serialize());
        }
        return serializedOneTimePreKeys;
    }


    private String serializeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) throws InvalidKeyException {
        return kyberPreKeyRecord.getId() + ":"
                + base64Encoder.encodeToString(kyberPreKeyRecord.getKeyPair().getPublicKey().serialize())
                + ":" + base64Encoder.encodeToString(kyberPreKeyRecord.getSignature());
    }

    private String[] serializeKyberPreKeys(KyberPreKeyRecord[] kyberPreKeyRecords) throws InvalidKeyException {
        String[] serializedKyberPreKeys = new String[kyberPreKeyRecords.length];
        int idx = 0;
        for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords) {
            serializedKyberPreKeys[idx++] = serializeKyberPreKey(kyberPreKeyRecord);
        }
        return serializedKyberPreKeys;
    }

    public void processKeyDistributionMessage(String name, String base64Key) {
        try {
            Log.i("SignalProtocolService", "base64 keyDistributionMessage: " + base64Key);
            SenderKeyDistributionMessage senderKeyDistributionMessage =
                    new SenderKeyDistributionMessage(Base64.getDecoder().decode(base64Key));

            new GroupSessionBuilder(signalProtocolStore)
                    .process(new SignalProtocolAddress(name, deviceId), senderKeyDistributionMessage);

            GroupCipher groupCipher = new GroupCipher(signalProtocolStore, new SignalProtocolAddress(name, deviceId));
            groupCipherTable.put(name, groupCipher);
            Log.i("SignalProtocolService", "Group chat chipper created and stored : " + name);
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error during decrypt key distribution message", e);
        }
    }


    private void decryptMessage(String base64Message, String sender, String exchangeName, String messageType) {
        try {
            switch (messageType.toLowerCase()) {
                case "advert":
                    processAdvertisementMessage(base64Message, exchangeName);
                    break;
                case "private":
                case "key_dist":
                    processPrivateMessage(base64Message, sender, exchangeName, messageType);
                    break;
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during decrypt message.", e);
        }
    }

    private void processAdvertisementMessage(String base64Message, String exchangeName) throws NoSessionException,
            InvalidMessageException, DuplicateMessageException, LegacyMessageException {
        String sender = exchangeName.split("_")[0];
        GroupCipher groupCipher = groupCipherTable.get(sender);
        Log.i("SignalProtocolService", "Decrypted advertisement exchangeName: " + exchangeName
                + " sender :" + sender);
        if (groupCipher != null) {
            String message = new String(groupCipher.decrypt(base64Decoder.decode(base64Message)));
            Log.i("SignalProtocolService", "Decrypted advertisement message: " + message);
            dbHelper.storeDecryptedMessage(sender, message);
            broadcastNewMessageReceived();
            publishNotification(sender, message);
        }
    }

    private void processPrivateMessage(String base64Message, String sender, String exchangeName, String messageType) throws InvalidMessageException,
            InvalidVersionException, LegacyMessageException, InvalidKeyException,
            UntrustedIdentityException, DuplicateMessageException, InvalidKeyIdException {

        PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender, deviceId);
        SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, signalProtocolAddress);
        String message = new String(sessionCipher.decrypt(preKeySignalMessage));

        if (messageType.equalsIgnoreCase("key_dist")) {
            processKeyDistributionMessage(sender, message);
        } else {
            Log.i("SignalProtocolService", "Decrypted private message: " + message);
            dbHelper.storeDecryptedMessage(exchangeName.split("_")[0], message);
            broadcastNewMessageReceived();
            publishNotification(sender, message);
        }
    }

    private void publishNotification(String sender, String text) {
        Notification notification = new NotificationCompat
                .Builder(SignalProtocolService.this, Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
                .setContentTitle("Message from " + sender)
                .setLargeIcon(BitmapFactory
                        .decodeResource(getResources(), R.drawable.ic_notif_sekretess))
                .setContentText(text.substring(0, Math.min(10, text.length()))
                        .concat("...")).setSmallIcon(R.drawable.ic_notif_sekretess)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(getApplicationContext());
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME,
                "New message", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification);
        }
    }

    private void broadcastNewMessageReceived() {
        Log.i("SignalProtocolService", "Sending new-incoming-message event");
        Intent intent = new Intent(Constants.EVENT_NEW_INCOMING_MESSAGE);
        intent.setAction(Constants.EVENT_NEW_INCOMING_MESSAGE);
        sendBroadcast(intent);
    }

    private void broadcastSignupFailed() {
        Log.i("SignalProtocolService", "Sending signup-failed event");
        Intent intent = new Intent(Constants.EVENT_SIGNUP_FAILED);
        intent.setAction(Constants.EVENT_SIGNUP_FAILED);
        sendBroadcast(intent);
    }

}
