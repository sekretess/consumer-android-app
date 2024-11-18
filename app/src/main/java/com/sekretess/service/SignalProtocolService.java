package com.sekretess.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sekretess.Constants;
import com.sekretess.MainActivity;
import com.sekretess.R;
import com.sekretess.dto.KeyMaterial;
import com.sekretess.dto.RegistrationAndDeviceId;
import com.sekretess.repository.DbHelper;
import com.sekretess.repository.SekretessSignalProtocolStore;
import com.sekretess.ui.ChatsActivity;
import com.sekretess.ui.LoginActivity;
import com.sekretess.utils.KeycloakManager;

import net.openid.appauth.AuthState;

import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SessionCipher;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.Curve;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.KyberPreKeyStore;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.signal.libsignal.protocol.util.Medium;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SignalProtocolService extends SekretessBackgroundService {
    private static SekretessSignalProtocolStore signalProtocolStore;
    private int deviceId;
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private DbHelper dbHelper;

    public static final int SIGNAL_PROTOCOL_NOTIFICATION = 2;
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);

    private final BroadcastReceiver encryptedMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String encryptedMessage = intent.getStringExtra("encryptedMessage");
            String sender = intent.getStringExtra("sender");
            decryptMessage(encryptedMessage, sender, deviceId);
        }
    };

    private final BroadcastReceiver loginEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (signalProtocolStore == null) {
                    Log.w("SignalProtocolService", "Signal protocol store is null. Initializing protocolStore...");
                    IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
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

                        PreKeyRecord[] preKeyRecords = dbHelper.getPreKeyRecords();
                        for (PreKeyRecord preKeyRecord : preKeyRecords) {
                            signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
                        }
                        dbHelper.loadSessions(signalProtocolStore);
                    }
                } else {
                    for (SignedPreKeyRecord signedPreKeyRecord : signalProtocolStore.loadSignedPreKeys()) {
                        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());

                    }
                }
            } catch (Exception e) {
                Log.e("SignalProtocolService",
                        "Something wrong gone during handle login event. No cryptographic env initialized!", e);

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
            Base64.Encoder encoder = Base64.getEncoder();

            try {
                KeyMaterial keyMaterial = initializeKeys();
                boolean result = KeycloakManager.getInstance().createUser(username, email, password,
                        keyMaterial.getRegistrationId(),
                        encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey().serialize()),
                        encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair().getPublicKey().serialize()),
                        keyMaterial.getOpk(),
                        encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()),
                        String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));

                if (result) {
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

        Log.i("SignalProtocolService", "All broadcastreceivers registered");
        Log.i("SignalProtocolService", "signalProtocolStore = " + signalProtocolStore);
        if (signalProtocolStore == null) {
            Log.i("SignalProtocolService", "SignalProtocolStore is null. Starting logging in process");
            startLoginActivity();
        }
        dbHelper = new DbHelper(this);
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
        PreKeyRecord[] opk = generateSignedPreKeys(15);
        dbHelper.storePreKeyRecords(opk);
        return new KeyMaterial(dbHelper.getRegistrationId().getRegistrationId(), serializeSignedPreKeys(opk),
                dbHelper.getSignedPreKeyRecord(), dbHelper.getIdentityKeyPair(),
                dbHelper.getSignedPreKeyRecord().getSignature());
    }

    private KeyMaterial initializeKeys() throws InvalidKeyException {

        ECKeyPair ecKeyPair = Curve.generateKeyPair();
        IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
        IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

        int registrationId = KeyHelper.generateRegistrationId(false);
        signalProtocolStore = new SekretessSignalProtocolStore(getApplicationContext(), identityKeyPair, registrationId);
        //Generate signed prekeyRecord
        ECKeyPair keyPair = Curve.generateKeyPair();
        byte[] signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(), keyPair.getPublicKey().serialize());

        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyId,
                System.currentTimeMillis(), keyPair, signature);
//      Generate post quantum resistance keys
        KEMKeyPair kemKeyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        int kyberSignedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        KyberPreKeyRecord kyberPreKeyRecord = new KyberPreKeyRecord(kyberSignedPreKeyId,
                System.currentTimeMillis(), kemKeyPair, signature);
//      Generated post quantum keys
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

        //Generate one-time prekeys
        PreKeyRecord[] opk = generateSignedPreKeys(15);
        this.deviceId = Math.abs(new Random().nextInt(Medium.MAX_VALUE - 1));
        Log.i("SignalProtocolService", "Keys initialized");
        dbHelper.storeIdentityKeyPair(identityKeyPair);
        dbHelper.storePreKeyRecords(opk);
        dbHelper.storeRegistrationId(registrationId, deviceId);
        dbHelper.storeSignedPreKeyRecord(signedPreKeyRecord);

        return new KeyMaterial(registrationId, serializeSignedPreKeys(opk), signedPreKeyRecord, identityKeyPair, signature);
    }

    public PreKeyRecord[] generateSignedPreKeys(int count) {
        PreKeyRecord[] preKeyRecords = new PreKeyRecord[count];
        SecureRandom preKeyRecordIdGenerator = new SecureRandom();
        for (int i = 0; i < count; i++) {
            int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
            ECKeyPair ecKeyPair = Curve.generateKeyPair();
            PreKeyRecord preKeyRecord = new PreKeyRecord(id, ecKeyPair);
            signalProtocolStore.storePreKey(id, preKeyRecord);
            preKeyRecords[i] = preKeyRecord;
        }
        return preKeyRecords;
    }

    public String[] serializeSignedPreKeys(PreKeyRecord[] preKeyRecords) throws InvalidKeyException {
        String[] oneTimePreKeys = new String[preKeyRecords.length];
        Base64.Encoder encoder = Base64.getEncoder();
        int idx = 0;
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            oneTimePreKeys[idx++] = preKeyRecord.getId() + ":" + encoder.encodeToString(preKeyRecord.getKeyPair().getPublicKey().serialize());
        }
        return oneTimePreKeys;
    }

    public void decryptMessage(String base64Message, String name, int deviceId) {
        try {
            PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
            SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(name, deviceId);
            SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, signalProtocolAddress);
            String message = new String(sessionCipher.decrypt(preKeySignalMessage));

            Log.i("SignalProtocolService", "Decrypted message: " + message);
            dbHelper.storeDecryptedMessage(name, message);
            broadcastNewMessageReceived();

            String channelId = "sekretess_notif";
            Notification notification = new NotificationCompat.Builder(SignalProtocolService.this, channelId).setContentTitle("Message from " + name).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_sekretess)).setContentText(message.substring(0, Math.min(10, message.length())).concat("...")).setSmallIcon(R.drawable.ic_notif_sekretess).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(m, notification);
            }


        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during decrypt message.", e);
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
