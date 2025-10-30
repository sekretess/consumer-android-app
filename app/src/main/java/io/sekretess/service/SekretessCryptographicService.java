package io.sekretess.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.signal.libsignal.protocol.UsePqRatchet;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.GroupChatDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.KyberPreKeyRecords;
import io.sekretess.dto.MessageDto;
import io.sekretess.dto.RegistrationAndDeviceId;
import io.sekretess.enums.MessageType;
import io.sekretess.repository.DbHelper;
import io.sekretess.repository.SekretessSignalProtocolStore;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.NotificationPreferencesUtils;


public class SekretessCryptographicService {
    private final int SIGNAL_KEY_COUNT = 15;
    private static SekretessSignalProtocolStore signalProtocolStore;
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final Map<String, GroupCipher> groupCipherTable = new ConcurrentHashMap<>();

    private final String TAG = "SekretessCryptographicService";
    private final Context context;
    private final int deviceId = 1;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SekretessCryptographicService(Context context) {
        this.context = context;
        initSignalProtocol();
    }

    public void updateOneTimeKeys() {
        try (DbHelper dbHelper = new DbHelper(context)) {
            IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
            PreKeyRecord[] preKeyRecords = generatePreKeys();
            KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());
            String[] strPreKeyRecords = serializeSignedPreKeys(preKeyRecords);
            String[] strKyberPreKeyRecords = serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords());
            if (ApiClient.updateOneTimeKeys(context, dbHelper.getAuthState(), strPreKeyRecords, strKyberPreKeyRecords)) {
                storePreKeyRecords(preKeyRecords);
                storeKyberPreKeyRecords(kyberPreKeyRecords);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during update one time keys", e);
        }
    }


    private KyberPreKeyRecords generateKyberPreKeys(ECPrivateKey ecPrivateKey) {
        // Generate post quantum resistance keys
        KyberPreKeyRecord[] kyberPreKeyRecords = new KyberPreKeyRecord[SIGNAL_KEY_COUNT];

        for (int i = 0; i < kyberPreKeyRecords.length; i++) {
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
        KyberPreKeyRecord kyberPreKeyRecord = new KyberPreKeyRecord(kyberSignedPreKeyId, System.currentTimeMillis(), kemKeyPair, ecPrivateKey.calculateSignature(kemKeyPair.getPublicKey().serialize()));
        return kyberPreKeyRecord;
    }

    public void initProtocolStorage(String username) {
        Log.i(TAG, "Login event received");
        try (DbHelper dbHelper = new DbHelper(context)) {
            SharedPreferences globalVariables = context.getSharedPreferences("global-variables", Context.MODE_PRIVATE);
            globalVariables.edit().putString("username", username).apply();

            if (signalProtocolStore == null) {
                Log.w(TAG, "Signal protocol store is null. Initializing protocolStore...");
                IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
                if (identityKeyPair == null) {
                    Log.w(TAG, "No cryptographic keys found. Initializing keys...");
                    initializeSecretKeys(keyMaterial -> ApiClient.upsertKeyStore(context, keyMaterial));
                } else {
                    Log.w(TAG, "Cryptographic keys found. Loading from database...");
                    loadCryptoKeysFromDb(context);
                }
            } else {
                for (SignedPreKeyRecord signedPreKeyRecord : signalProtocolStore.loadSignedPreKeys()) {
                    Log.i(TAG, "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
                    Log.i(TAG, "SignedPrekeyRecordLoaded. Signature:" + base64Encoder.encodeToString(signedPreKeyRecord.getSignature()));
                }
            }
            if (groupCipherTable.isEmpty()) {
                List<GroupChatDto> groupChatsInfo = dbHelper.getGroupChatsInfo();
                for (GroupChatDto groupChatDto : groupChatsInfo) {
                    processKeyDistributionMessage(groupChatDto.getSender(), groupChatDto.getDistributionKey());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Something wrong gone during handle login event. No cryptographic env initialized!", e);
            context.sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));

        }
    }

    public void processKeyDistributionMessage(String name, String base64Key) {
        try {
            Log.i(TAG, "base64 keyDistributionMessage: " + base64Key);
            SenderKeyDistributionMessage senderKeyDistributionMessage = new SenderKeyDistributionMessage(Base64.getDecoder().decode(base64Key));
            new GroupSessionBuilder(signalProtocolStore).process(new SignalProtocolAddress(name, 1), senderKeyDistributionMessage);
            GroupCipher groupCipher = new GroupCipher(signalProtocolStore, new SignalProtocolAddress(name, 1));
            groupCipherTable.put(name, groupCipher);
            Log.i(TAG, "Group chat chipper created and stored : " + name);
        } catch (Exception e) {
            Log.e(TAG, "Error during decrypt key distribution message", e);
            Toast.makeText(context, "Error during decrypt distribution message" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initSignalProtocol() {
        try (DbHelper dbHelper = new DbHelper(context)) {
            Log.i(TAG, "All broadcast receivers registered");
            Log.i(TAG, "signalProtocolStore = " + signalProtocolStore);
            if (signalProtocolStore == null) {
                if (dbHelper.getIdentityKeyPair() != null) {
                    Log.i(TAG, "SignalProtocolStore is null. Loading data from db");
                    try {
                        loadCryptoKeysFromDb(context);
                    } catch (Exception e) {
                        Log.e(TAG, "SignalProtocolStore is null. Error on load from DB.", e);
                        broadcastTokenIssue();
                    }
                } else {
                    Log.i(TAG, "SignalProtocolStore is null. Starting logging in process");
                }
            }
        }
    }

    private void storeKyberPreKeyRecords(KyberPreKeyRecords kyberPreKeyRecords) {
        try (DbHelper dbHelper = new DbHelper(context)) {

            for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords.getKyberPreKeyRecords()) {
                dbHelper.storeKyberPreKey(kyberPreKeyRecord);
                signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);
            }

            dbHelper.storeKyberPreKey(kyberPreKeyRecords.getLastResortKyberPreKeyRecord());
            signalProtocolStore.storeKyberPreKey(kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId(), kyberPreKeyRecords.getLastResortKyberPreKeyRecord());
        }
    }

    private void storePreKeyRecords(PreKeyRecord[] preKeyRecords) {
        try (DbHelper dbHelper = new DbHelper(context)) {
            for (PreKeyRecord preKeyRecord : preKeyRecords) {
                signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
                dbHelper.storePreKeyRecord(preKeyRecord);
            }
        }
    }

    private void storeSignedPreKey(SignedPreKeyRecord signedPreKeyRecord) {
        try (DbHelper dbHelper = new DbHelper(context)) {
            signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
            dbHelper.storeSignedPreKeyRecord(signedPreKeyRecord);
        }
    }

    private SignedPreKeyRecord generateSignedPreKey(ECKeyPair keyPair, byte[] signature) {
        //Generate signed prekeyRecord
        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private PreKeyRecord[] generatePreKeys() {
        PreKeyRecord[] preKeyRecords = new PreKeyRecord[SIGNAL_KEY_COUNT];
        SecureRandom preKeyRecordIdGenerator = new SecureRandom();
        for (int i = 0; i < preKeyRecords.length; i++) {
            int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
            ECKeyPair ecKeyPair = ECKeyPair.generate();
            PreKeyRecord preKeyRecord = new PreKeyRecord(id, ecKeyPair);
            preKeyRecords[i] = preKeyRecord;
        }
        return preKeyRecords;
    }

    private String[] serializeSignedPreKeys(PreKeyRecord[] preKeyRecords) throws InvalidKeyException {
        String[] serializedOneTimePreKeys = new String[preKeyRecords.length];

        int idx = 0;
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            serializedOneTimePreKeys[idx++] = preKeyRecord.getId() + ":" + base64Encoder.encodeToString(preKeyRecord.getKeyPair().getPublicKey().serialize());
        }
        return serializedOneTimePreKeys;
    }


    private String serializeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) throws InvalidKeyException {
        return kyberPreKeyRecord.getId() + ":" + base64Encoder.encodeToString(kyberPreKeyRecord.getKeyPair().getPublicKey().serialize()) + ":" + base64Encoder.encodeToString(kyberPreKeyRecord.getSignature());
    }

    private String[] serializeKyberPreKeys(KyberPreKeyRecord[] kyberPreKeyRecords) throws InvalidKeyException {
        String[] serializedKyberPreKeys = new String[kyberPreKeyRecords.length];
        int idx = 0;
        for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords) {
            serializedKyberPreKeys[idx++] = serializeKyberPreKey(kyberPreKeyRecord);
        }
        return serializedKyberPreKeys;
    }

    public void initializeSecretKeys(Function<KeyMaterial, Boolean> f) {
        try {
//            ECKeyPair ecKeyPair = ECKeyPair.generate();
//            IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
//            IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());
            IdentityKeyPair identityKeyPair = IdentityKeyPair.generate();

            int registrationId = KeyHelper.generateRegistrationId(false);
            ECKeyPair signedPreKeyPair = ECKeyPair.generate();

            byte[] signature = identityKeyPair.getPrivateKey().calculateSignature(signedPreKeyPair.getPublicKey().serialize());

            //Generate one-time prekeys
            PreKeyRecord[] opk = generatePreKeys();

            SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(signedPreKeyPair, signature);
            KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());

            KeyMaterial keyMaterial = new KeyMaterial(registrationId, serializeSignedPreKeys(opk), signedPreKeyRecord, identityKeyPair, signature, serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords()), base64Encoder.encodeToString(kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getKeyPair().getPublicKey().serialize()), kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getSignature(), kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId());

            if (f.apply(keyMaterial)) {
                try (DbHelper dbHelper = new DbHelper(context)) {
                    dbHelper.clearKeyData();
                    dbHelper.storeIdentityKeyPair(identityKeyPair);
                    dbHelper.storeRegistrationId(registrationId, deviceId);

                    signalProtocolStore = new SekretessSignalProtocolStore(context, identityKeyPair, registrationId);

                    storeKyberPreKeyRecords(kyberPreKeyRecords);
                    storePreKeyRecords(opk);
                    storeSignedPreKey(signedPreKeyRecord);
                }
            }


        } catch (Exception e) {
            Log.i(TAG, "KeyMaterial generation failed", e);
            Toast.makeText(context, "KeyMaterial generation failed " + e.getMessage(), Toast.LENGTH_LONG).show();
            context.sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
        }
    }

    private void loadCryptoKeysFromDb(Context context) throws InvalidMessageException {
        try (DbHelper dbHelper = new DbHelper(context)) {
            RegistrationAndDeviceId registrationId = dbHelper.getRegistrationId();
            IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
            signalProtocolStore = new SekretessSignalProtocolStore(context, identityKeyPair, registrationId.getRegistrationId());
            SignedPreKeyRecord signedPreKeyRecord = dbHelper.getSignedPreKeyRecord();
            Log.i(TAG, "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
            Log.i(TAG, "SignedPrekeyRecordLoaded. Signature:" + base64Encoder.encodeToString(signedPreKeyRecord.getSignature()));
            signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

            List<GroupChatDto> groupChatsInfo = dbHelper.getGroupChatsInfo();
            for (GroupChatDto groupChatDto : groupChatsInfo) {
                processKeyDistributionMessage(groupChatDto.getSender(), groupChatDto.getDistributionKey());
            }

            dbHelper.loadPreKeyRecords(signalProtocolStore);
            dbHelper.loadSessions(signalProtocolStore);
            dbHelper.loadKyberPreKeys(signalProtocolStore);
        }
    }

    public void decryptMessage(String messageText) {
        try {
            String exchangeName = "";
            Log.i(TAG, "Received payload:" + messageText + " ExchangeName :");
            MessageDto message = objectMapper.readValue(messageText, MessageDto.class);
            String encryptedText = message.getText();
            MessageType messageType = MessageType.getInstance(message.getType());
            String sender = "";
            switch (messageType) {
                case ADVERTISEMENT:
                    exchangeName = message.getBusinessExchange();
                    processAdvertisementMessage(encryptedText, exchangeName);
                    break;
                case KEY_DISTRIBUTION:
                case PRIVATE:
                    exchangeName = message.getConsumerExchange();
                    sender = message.getSender();
                    Log.i(TAG, "Private message received. Sender:" + sender + " Exchange:" + exchangeName);
                    processPrivateMessage(encryptedText, sender, messageType);
                    break;
            }
            Log.i(TAG, "Encoded message received : " + message);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void decryptMessage(byte[] body) {
        this.decryptMessage(new String(body));
    }

    private void processAdvertisementMessage(String base64Message, String exchangeName) throws NoSessionException, InvalidMessageException, DuplicateMessageException, LegacyMessageException {
        String sender = exchangeName.split("_")[0];
        handleAdvertisementMessage(sender, base64Message, exchangeName, (message) -> {
            Log.i("SignalProtocolService", "Decrypted advertisement message: " + message);
            try (DbHelper dbHelper = new DbHelper(context)) {
                dbHelper.storeDecryptedMessage(sender, message);
                broadcastNewMessageReceived();
                publishNotification(sender, message);
            }
        });
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) throws InvalidMessageException, InvalidVersionException, LegacyMessageException, InvalidKeyException, UntrustedIdentityException, DuplicateMessageException, InvalidKeyIdException {
        handlePrivateMessage(sender, base64Message, (message) -> {
            try (DbHelper dbHelper = new DbHelper(context)) {
                if (messageType == MessageType.KEY_DISTRIBUTION) {
                    processKeyDistributionMessage(sender, message);
                    //Store group chat info
                    dbHelper.storeGroupChatInfo(message, sender);
                } else {
                    Log.i("SignalProtocolService", "Decrypted private message: " + message);
                    dbHelper.storeDecryptedMessage(sender, message);
                    publishNotification(sender, message);
                    broadcastNewMessageReceived();
                }
            }
        });
    }

    private void publishNotification(String sender, String text) {
        Intent intent = new Intent();
        var notification = new NotificationCompat
                .Builder(context, Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
                .setContentTitle("Message from " + sender)
                .setSilent(false)
                .setLargeIcon(BitmapFactory
                        .decodeResource(context.getResources(), R.drawable.ic_notif_sekretess))
                .setContentText(text.substring(0, Math.min(10, text.length())).concat("..."))
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME, "New message", NotificationManager.IMPORTANCE_HIGH);
        channel.setAllowBubbles(true);
        channel.enableVibration(NotificationPreferencesUtils.getVibrationPreferences(context, sender));
        boolean soundAlerts = NotificationPreferencesUtils.getSoundAlertsPreferences(context, sender);
        Log.i("SekretessRabbitMqService", "soundAlerts:" + soundAlerts + "sender:" + sender);
        if (!soundAlerts) {
            notification.setSilent(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        } else {
            notification.setDefaults(0);
            notification.setSilent(false);
        }
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification.build());
        }
    }

    private void broadcastNewMessageReceived() {
        Log.i("SignalProtocolService", "Sending new-incoming-message event");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        intent.setAction(Constants.EVENT_NEW_INCOMING_MESSAGE);
        context.sendBroadcast(intent);
    }

    private void handleAdvertisementMessage(String sender, String base64Message,
                                            String exchangeName, Consumer<String> supplier) throws NoSessionException, InvalidMessageException, DuplicateMessageException, LegacyMessageException {
        GroupCipher groupCipher = groupCipherTable.get(sender);
        Log.i(TAG, "Decrypted advertisement exchangeName: " + exchangeName + " sender :" + sender);
        if (groupCipher != null) {
            supplier.accept(new String(groupCipher.decrypt(base64Decoder.decode(base64Message))));
        } else {
            Log.i(TAG, "No group cipher available : " + exchangeName + " sender :" + sender);
            Looper.getMainLooper();
            Handler mainHander = new Handler(Looper.getMainLooper());
            mainHander.post(() -> {
                Toast.makeText(context, "No group cipher available : " + exchangeName + " sender :" + sender, Toast.LENGTH_LONG).show();
            });
        }
    }

    private void handlePrivateMessage(String sender, String base64Message, Consumer<String> consumer) {
        try {
            PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
            SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender, 1);
            SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, signalProtocolAddress);
            Log.i(TAG, "" + signalProtocolStore);
            String message = new String(sessionCipher.decrypt(preKeySignalMessage, UsePqRatchet.YES));
            consumer.accept(message);
        } catch (Exception e) {
            Log.e(TAG, "Error during decrypt private message", e);
            Toast.makeText(context, "Error during decrypt private message" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void broadcastTokenIssue() {
        context.sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
    }
}
