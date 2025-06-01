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
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

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

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.KyberPreKeyRecords;
import io.sekretess.dto.MessageDto;
import io.sekretess.dto.RegistrationAndDeviceId;
import io.sekretess.repository.DbHelper;
import io.sekretess.repository.SekretessSignalProtocolStore;
import io.sekretess.ui.LoginActivity;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.KeycloakManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SekretessRabbitMqService extends SekretessBackgroundService {
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static SekretessSignalProtocolStore signalProtocolStore;
    private int deviceId;
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final Map<String, GroupCipher> groupCipherTable = new ConcurrentHashMap<>();

    public static final int RABBIT_MQ_NOTIFICATION = 1;
    private static final String TAG = "SekretessRabbitMqConsumer";
    private Channel rabbitMqChannel;
    private Connection rabbitMqConnection;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);
    private Thread rabbitMqConnectorThread;


    private void closeRabbitMqConnections() {
        if (rabbitMqChannel != null) {
            try {
                rabbitMqChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        if (rabbitMqConnection != null) {
            try {
                rabbitMqConnection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void destroyed() {
        Log.i(this.getClass().getName(), "Destroyed");
        serviceInstances.getAndSet(0);
        Executors.newSingleThreadExecutor().submit(this::closeRabbitMqConnections);
        if (rabbitMqConnectorThread != null)
            rabbitMqConnectorThread.interrupt();
    }

    @Override
    public void started(Intent intent) {
        Log.i("SekretessRabbitMqService", "SekretessRabbitMqService started successfully");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        registerBroadcastReceivers();
        initSignalProtocol();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Connection createConnection() {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost("sekretess");


        while (true) {

            try {
                Thread.sleep(3000);
                DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
                String amqpConnectionUrl = getString(R.string.rabbit_mq_uri);
                amqpConnectionUrl = String.format(amqpConnectionUrl, dbHelper.getUserNameFromJwt(), dbHelper.getAuthState().getAccessToken());
                Log.i("SekretessRabbitMqService", "Connecting with URI: " + amqpConnectionUrl);
                connectionFactory.setUri(amqpConnectionUrl);

                connectionFactory.setAutomaticRecoveryEnabled(false);

                Connection connection = connectionFactory.newConnection();
                connection.addShutdownListener(cause -> {
                    try {
                        Log.e("SekretessRabbitMqService", "AMQP Connection shutdown. Trying reconnect...");
                        rabbitMqConnection = createConnection();
                        rabbitMqChannel = rabbitMqConnection.createChannel();
                    } catch (Exception e) {
                        Log.e("SekretessRabbitMqService", "AMQP Connection creation establishment failed", e);
                    }
                });
                return connection;
            } catch (Exception e) {
                Log.e("SekretessRabbitMqService", "Can not establish connection", e);
            }
        }
    }

    private Channel createChannel(Connection connection) {
        while (connection.isOpen()) {
            try {
                Channel channel = connection.createChannel();
                channel.addShutdownListener(cause -> {
                    rabbitMqChannel = createChannel(connection);
                });
                return channel;
            } catch (Exception e) {
                Log.e("SekretessRabbitMqService", "AMQP channel creation failed", e);
            }
        }
        return null;
    }

    private void startConsumeQueue(String queueName) {

        try {
            if (rabbitMqChannel == null || !rabbitMqChannel.getConnection().isOpen()) {
                rabbitMqConnection = createConnection();
                rabbitMqChannel = createChannel(rabbitMqConnection);
                rabbitMqChannel.confirmSelect();

                Log.i("SekretessRabbitMqService", "RabbitMq Consumer connection established.");
                rabbitMqChannel.basicConsume(queueName.concat(Constants.RABBIT_MQ_CONSUMER_QUEUE_SUFFIX),
                        true, new DefaultConsumer(rabbitMqChannel) {
                            @Override
                            public void handleDelivery(String consumerTag, Envelope envelope,
                                                       AMQP.BasicProperties properties, byte[] body) {
                                try {
                                    String exchangeName = envelope.getExchange();
                                    Log.i("SekretessRabbitMqService", "Received payload:" + new String(body));
                                    MessageDto message = objectMapper.readValue(body, MessageDto.class);
                                    String encryptedText = message.getText();
                                    String messageType = message.getType();
                                    String sender = "";
                                    switch (messageType.toLowerCase()) {
                                        case "advert":
                                            exchangeName = message.getBusinessExchange();
                                            break;
                                        case "key_dist":
                                            exchangeName = message.getConsumerExchange();
                                            sender = message.getSender();
                                            break;
                                        case "private":
                                            exchangeName = message.getConsumerExchange();
                                            break;
                                    }


                                    Log.i("SekretessRabbitMqService", "Encoded message received : " + message);
//

                                    broadcastIncomingMessage(encryptedText, exchangeName,
                                            messageType, sender);

                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                        });
                Log.i("SekretessRabbitMqService", "RabbitMq Consumer started");
            } else {
                Log.i("SekretessRabbitMqService", "RabbitMq Consumer already started");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    private void broadcastIncomingMessage(String encryptedText, String exchangeName,
                                          String messageType, String sender) {
        Intent intent = new Intent(Constants.EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE);
        intent.putExtra("encryptedText", encryptedText);
        intent.putExtra("exchangeName", exchangeName);
        intent.putExtra("messageType", messageType);
        intent.putExtra("sender", sender);
        sendStickyBroadcast(intent);

    }

    @Override
    public String getChannelId() {
        return "sekretess:rb-service-channel";
    }

    @Override
    public int getNotificationId() {
        return RABBIT_MQ_NOTIFICATION;
    }


    public void onNewEncryptedMessage(String encryptedMessage, String exchangeName,
                                      String messageType, String sender) {
        decryptMessage(encryptedMessage, sender, exchangeName, messageType);
    }


    public void initProtocolStorage(String username) {
        Log.i("SignalProtocolService", "Login event received");
        try {
            SharedPreferences globalVariables = getApplicationContext()
                    .getSharedPreferences("global-variables", MODE_PRIVATE);
            globalVariables
                    .edit()
                    .putString("username", username)
                    .apply();
            DbHelper dbHelper = DbHelper.getInstance(this);
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
                    updateOneTimeKeys();
                } else {
                    Log.w("SignalProtocolService", "Cryptographic keys found. Loading from database...");
                    loadCryptoKeysFromDb(getApplicationContext());
                }
            } else {
                for (SignedPreKeyRecord signedPreKeyRecord : signalProtocolStore.loadSignedPreKeys()) {
                    Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
                    Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Signature:" + base64Encoder.encodeToString(signedPreKeyRecord.getSignature()));

                }
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Something wrong gone during handle login event. No cryptographic env initialized!", e);

        }
    }


    private void loadCryptoKeysFromDb(Context context) throws InvalidMessageException {
        DbHelper dbHelper = DbHelper.getInstance(this);
        RegistrationAndDeviceId registrationId = dbHelper.getRegistrationId();
        IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
        signalProtocolStore = new SekretessSignalProtocolStore(context, identityKeyPair, registrationId.getRegistrationId());
        SignedPreKeyRecord signedPreKeyRecord = dbHelper.getSignedPreKeyRecord();
        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Signature:" + base64Encoder.encodeToString(signedPreKeyRecord.getSignature()));
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
        deviceId = registrationId.getDeviceId();

        dbHelper.loadPreKeyRecords(signalProtocolStore);
        dbHelper.loadSessions(signalProtocolStore);
        dbHelper.loadKyberPreKeys(signalProtocolStore);
    }


    public void initializeKeys(String username, String email, String password) {
        Log.i("SignalProtocolService", "Initialize event received");


        try {
            KeyMaterial keyMaterial = initializeKeys();

            if (KeycloakManager.getInstance()
                    .createUser(username, email, password, keyMaterial)) {
                startLoginActivity();
            } else {
                broadcastSignupFailed("");
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during initialize user", e);
            broadcastSignupFailed(e.getMessage());
        }
    }

    private void startLoginActivity() {
        Intent loginActivityIntent = new Intent(SekretessRabbitMqService.this, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginActivityIntent);
    }

    private void registerBroadcastReceivers() {
        try {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("SignalProtocolService", "Update event received");
                    try {
                        updateOneTimeKeys();
                    } catch (Exception e) {
                        Log.e("SignalProtocolService", "Error occurred during update OPK", e);
                    }
                }
            }, new IntentFilter(Constants.EVENT_UPDATE_KEY), RECEIVER_EXPORTED);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String encryptedText = intent.getStringExtra("encryptedText");
                    String exchangeName = intent.getStringExtra("exchangeName");
                    String messageType = intent.getStringExtra("messageType");
                    String sender = intent.getStringExtra("sender");
                    onNewEncryptedMessage(encryptedText, exchangeName, messageType, sender);
                }
            }, new IntentFilter(Constants.EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE), RECEIVER_EXPORTED);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("SekretessRabbitMqService", "Login event received");
                    String userName = intent.getStringExtra("userName");
                    rabbitMqConnectorThread = new Thread(() -> startConsumeQueue(userName));
                    rabbitMqConnectorThread.start();
                    initProtocolStorage(userName);
                }
            }, new IntentFilter(Constants.EVENT_LOGIN), RECEIVER_EXPORTED);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String email = intent.getStringExtra("email");
                    String username = intent.getStringExtra("username");
                    String password = intent.getStringExtra("password");
                    initializeKeys(username, email, password);
                }
            }, new IntentFilter(Constants.EVENT_INITIALIZE_KEY), RECEIVER_EXPORTED);
        } catch (Throwable t) {
            Log.i("SekretessRabbitMqService", "Error while register broadcastreceiver", t);
        }
    }

    private void initSignalProtocol() {
        DbHelper dbHelper = DbHelper.getInstance(this);
        Log.i("SignalProtocolService", "All broadcast receivers registered");
        Log.i("SignalProtocolService", "signalProtocolStore = " + signalProtocolStore);
        if (signalProtocolStore == null) {
            if (dbHelper.getIdentityKeyPair() != null) {
                Log.i("SignalProtocolService", "SignalProtocolStore is null. Loading data from db");
                try {
                    loadCryptoKeysFromDb(this);
                } catch (Exception e) {
                    Log.e("SignalProtocolService", "SignalProtocolStore is null. Error on load from DB.", e);
                    startLoginActivity();
                }
            } else {
                Log.i("SignalProtocolService", "SignalProtocolStore is null. Starting logging in process");
                startLoginActivity();
            }
        }
    }

    public void updateOneTimeKeys() throws InvalidKeyException {
        DbHelper dbHelper = DbHelper.getInstance(this);
        KeyMaterial keyMaterial = initializeKeys();
        if (KeycloakManager.getInstance().updateKeys(dbHelper.getAuthState(), keyMaterial)) {
            Toast.makeText(getApplicationContext(), "One time keys updated", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "One time keys update failed", Toast.LENGTH_LONG).show();
        }
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


        DbHelper dbHelper = DbHelper.getInstance(this);
        dbHelper.clearKeyData();
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
                kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getSignature(),
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
                System.currentTimeMillis(), kemKeyPair,
                ecPrivateKey.calculateSignature(kemKeyPair.getPublicKey().serialize()));
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);
        DbHelper dbHelper = DbHelper.getInstance(this);
        dbHelper.storeKyberPreKey(kyberPreKeyRecord);
        return kyberPreKeyRecord;
    }


    private SignedPreKeyRecord generateSignedPreKey(ECKeyPair keyPair, byte[] signature) {
        //Generate signed prekeyRecord
        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyId,
                System.currentTimeMillis(), keyPair, signature);
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

        DbHelper dbHelper = DbHelper.getInstance(this);
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
        DbHelper dbHelper = DbHelper.getInstance(this);
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
            Log.i("SignalProtocolService", "Decrypting message");
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
            DbHelper dbHelper = DbHelper.getInstance(this);
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
        Log.i("SignalProtocolService", "" + signalProtocolStore);
        String message = new String(sessionCipher.decrypt(preKeySignalMessage));

        if (messageType.equalsIgnoreCase("key_dist")) {
            processKeyDistributionMessage(sender, message);
        } else {
            Log.i("SignalProtocolService", "Decrypted private message: " + message);
            DbHelper dbHelper = DbHelper.getInstance(this);
            dbHelper.storeDecryptedMessage(exchangeName.split("_")[0], message);
            broadcastNewMessageReceived();
            publishNotification(sender, message);
        }
    }

    private void publishNotification(String sender, String text) {
        Notification notification = new NotificationCompat
                .Builder(SekretessRabbitMqService.this, Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
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
        Log.i("SignalProtocolService", "Sending new-incoming-message event.Context:" + getBaseContext());
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        intent.setAction(Constants.EVENT_NEW_INCOMING_MESSAGE);
        sendBroadcast(intent);

    }

    private void broadcastSignupFailed(String message) {
        Log.i("SignalProtocolService", "Sending signup-failed event");
        Intent intent = new Intent(Constants.EVENT_SIGNUP_FAILED);
        intent.putExtra("message", message);
        intent.setAction(Constants.EVENT_SIGNUP_FAILED);
        sendBroadcast(intent);
    }
}
