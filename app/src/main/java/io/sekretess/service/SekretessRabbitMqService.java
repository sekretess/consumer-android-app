package io.sekretess.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.impl.CredentialsProvider;

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

import io.sekretess.BuildConfig;
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
import io.sekretess.ui.LoginActivity;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.NotificationPreferencesUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SekretessRabbitMqService extends SekretessAbstractBackgroundService {
    private final int SIGNAL_KEY_COUNT = 15;
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
    private ScheduledExecutorService rabbitMqConnectionGuard;
    private String userName;

    private final BroadcastReceiver loginBroadcastEventReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("SekretessRabbitMqService", "Login event received");
                    userName = intent.getStringExtra("userName");
                    startRabbitMqConnectionGuard();
                    initProtocolStorage(userName);
                }
            };

    private void closeRabbitMqConnections() {
        try {
            Executors.newSingleThreadExecutor().submit(() -> {
                if (rabbitMqChannel != null) {
                    try {
                        rabbitMqChannel.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (TimeoutException e) {
                        Log.i("SekretessRabbitMqService", "RabbitMqChannel close failed", e);
                    }
                }
                if (rabbitMqConnection != null) {
                    try {
                        rabbitMqConnection.close();
                    } catch (IOException e) {
                        Log.i("SekretessRabbitMqService", "RabbitMqConnection close failed", e);
                    }


                }
            }).get();
        } catch (Throwable e) {
            Log.i("SekretessRabbitMqService", "RabbitMqConnection close failed", e);
        }


        if (rabbitMqConnectorThread != null)
            rabbitMqConnectorThread.interrupt();
        rabbitMqConnectorThread = null;

        if (rabbitMqConnectionGuard != null)
            rabbitMqConnectionGuard.shutdownNow();
        rabbitMqConnectionGuard = null;


    }

    private void startRabbitMqConnectionGuard() {
        if (rabbitMqConnectionGuard == null || rabbitMqConnectionGuard.isShutdown()
                || rabbitMqConnectionGuard.isTerminated()) {
            rabbitMqConnectionGuard = Executors.newScheduledThreadPool(1);
            rabbitMqConnectionGuard.scheduleWithFixedDelay(() -> {
                Log.i("SekretessRabbitMqService", "rabbitMqConnectionGuard...");
                if (isRabbitMqConnectionClose()) {
                    Log.i("SekretessRabbitMqService",
                            "rabbitMqConnectionGuard - RabbitMq connection not established connecting...");
                    startRabbitMqConnection();
                }
            }, 20, 20, TimeUnit.SECONDS);
        }
    }

    @Override
    public void destroyed() {
        Log.i(this.getClass().getName(), "Destroyed");
        serviceInstances.getAndSet(0);
        unregisterBroadcastReceiver();
        Executors.newSingleThreadExecutor().submit(this::closeRabbitMqConnections);
    }

    @Override
    public void started(Intent intent) {
        Log.i("SekretessRabbitMqService", "SekretessRabbitMqService started successfully");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        registerBroadcastReceivers();
        closeRabbitMqConnections();

        initSignalProtocol();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isRabbitMqConnectionClose() {
        return rabbitMqConnection == null || rabbitMqChannel == null || !rabbitMqConnection.isOpen() || !rabbitMqConnection.isOpen();
    }

    private void startRabbitMqConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost("sekretess");
        try {
            DbHelper dbHelper = new DbHelper(getApplicationContext());
            String amqpConnectionUrl = BuildConfig.RABBIT_MQ_URI;

            amqpConnectionUrl = String.format(amqpConnectionUrl, userName, dbHelper.getAuthState().getAccessToken());
            Log.i(TAG, "Connecting with URI: " + amqpConnectionUrl);
            connectionFactory.setUri(amqpConnectionUrl);
            connectionFactory.setCredentialsProvider(new CredentialsProvider() {
                @Override
                public String getUsername() {
                    return dbHelper.getUserNameFromJwt();
                }

                @Override
                public String getPassword() {
                    return dbHelper.getAuthState().getAccessToken();
                }
            });
            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.useSslProtocol();
            rabbitMqConnection = connectionFactory.newConnection();
            if (rabbitMqConnection == null) {
                Log.i(TAG, "RabbitMq Consumer connection NOT established.");
                return;
            }
            rabbitMqChannel = rabbitMqConnection.createChannel();
            if (rabbitMqChannel == null) {
                Log.i(TAG, "RabbitMq Consumer connection channel NOT established.");
                return;
            }
            rabbitMqChannel.confirmSelect();

            Log.i(TAG, "RabbitMq Consumer connection established.");
            rabbitMqChannel.basicConsume(userName.concat(Constants.RABBIT_MQ_CONSUMER_QUEUE_SUFFIX), true, new DefaultConsumer(rabbitMqChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    try {
                        String exchangeName = envelope.getExchange();
                        Log.i(TAG, "Received payload:" + new String(body) + " ExchangeName :" + exchangeName);
                        MessageDto message = objectMapper.readValue(body, MessageDto.class);
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
            });
            Log.i(TAG, "RabbitMq Consumer started");
        } catch (TimeoutException | NoSuchAlgorithmException | KeyManagementException |
                 URISyntaxException e) {
            Log.e(TAG, "Can not establish connection", e);
        } catch (IOException io) {
            Log.e(TAG, "Can not establish connection", io);

            closeRabbitMqConnections();
            sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
        }
    }

    @Override
    public String getChannelId() {
        return "sekretess:rb-service-channel";
    }

    @Override
    public int getNotificationId() {
        return RABBIT_MQ_NOTIFICATION;
    }


    public void initProtocolStorage(String username) {
        Log.i("SignalProtocolService", "Login event received");
        try {
            SharedPreferences globalVariables = getApplicationContext().getSharedPreferences("global-variables", MODE_PRIVATE);
            globalVariables.edit().putString("username", username).apply();
            DbHelper dbHelper = new DbHelper(this);
            if (signalProtocolStore == null) {
                Log.w("SignalProtocolService", "Signal protocol store is null. Initializing protocolStore...");
                IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
                if (identityKeyPair == null) {
                    Log.w("SignalProtocolService", "No cryptographic keys found. Initializing keys...");
                    initializeSecretKeys(keyMaterial -> ApiClient.upsertKeyStore(getApplicationContext(), keyMaterial, dbHelper.getAuthState().getIdToken()));
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
            if (groupCipherTable.isEmpty()) {
                List<GroupChatDto> groupChatsInfo = dbHelper.getGroupChatsInfo();
                for (GroupChatDto groupChatDto : groupChatsInfo) {
                    processKeyDistributionMessage(groupChatDto.getSender(), groupChatDto.getDistributionKey());
                }
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Something wrong gone during handle login event. No cryptographic env initialized!", e);
            sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));

        }
    }


    private void upsertKeyStore(KeyMaterial keyMaterial, String jwtToken) {
        if (ApiClient.upsertKeyStore(getApplicationContext(), keyMaterial, jwtToken)) {

        }
    }


    private void loadCryptoKeysFromDb(Context context) throws InvalidMessageException {
        DbHelper dbHelper = new DbHelper(this);
        RegistrationAndDeviceId registrationId = dbHelper.getRegistrationId();
        IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
        signalProtocolStore = new SekretessSignalProtocolStore(context, identityKeyPair, registrationId.getRegistrationId());
        SignedPreKeyRecord signedPreKeyRecord = dbHelper.getSignedPreKeyRecord();
        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Id:" + signedPreKeyRecord.getId());
        Log.i("SignalProtocolService", "SignedPrekeyRecordLoaded. Signature:" + base64Encoder.encodeToString(signedPreKeyRecord.getSignature()));
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
        deviceId = registrationId.getDeviceId();

        List<GroupChatDto> groupChatsInfo = dbHelper.getGroupChatsInfo();
        for (GroupChatDto groupChatDto : groupChatsInfo) {
            processKeyDistributionMessage(groupChatDto.getSender(), groupChatDto.getDistributionKey());
        }

        dbHelper.loadPreKeyRecords(signalProtocolStore);
        dbHelper.loadSessions(signalProtocolStore);
        dbHelper.loadKyberPreKeys(signalProtocolStore);
    }


    private void createConsumerUser(String username, String email, String password) {
        Log.i("SignalProtocolService", "Initialize event received");
        initializeSecretKeys(keyMaterial -> {
            if (ApiClient.createUser(getApplicationContext(), username, email, password, keyMaterial)) {
                startLoginActivity();
                return true;
            } else {
                return false;
            }
        });
    }

    private void startLoginActivity() {
        Intent loginActivityIntent = new Intent(SekretessRabbitMqService.this, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginActivityIntent);
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(loginBroadcastEventReceiver);
    }

    private void registerBroadcastReceivers() {
        try {
            Log.i("SignalProtocolService", "All broadcast receivers registered");
            IntentFilter keyUpdateFilter = new IntentFilter(Constants.EVENT_UPDATE_KEY);
            keyUpdateFilter.setPriority(100);
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
            }, keyUpdateFilter, RECEIVER_EXPORTED);

            IntentFilter newLoginFilter = new IntentFilter(Constants.EVENT_LOGIN);
            newLoginFilter.setPriority(100);
            registerReceiver(loginBroadcastEventReceiver, newLoginFilter, RECEIVER_EXPORTED);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String email = intent.getStringExtra("email");
                    String username = intent.getStringExtra("username");
                    String password = intent.getStringExtra("password");
                    createConsumerUser(username, email, password);
                }
            }, new IntentFilter(Constants.EVENT_SIGNUP), RECEIVER_EXPORTED);
        } catch (Throwable t) {
            Log.i("SekretessRabbitMqService", "Error while register broadcastreceiver", t);
        }
    }

    private void initSignalProtocol() {
        DbHelper dbHelper = new DbHelper(this);
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
            }
        }
    }

    public void updateOneTimeKeys() throws InvalidKeyException {
        DbHelper dbHelper = new DbHelper(this);
        IdentityKeyPair identityKeyPair = dbHelper.getIdentityKeyPair();
        PreKeyRecord[] preKeyRecords = generatePreKeys();
        KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());
        String[] strPreKeyRecords = serializeSignedPreKeys(preKeyRecords);
        String[] strKyberPreKeyRecords = serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords());
        if (ApiClient.updateOneTimeKeys(getApplicationContext(), dbHelper.getAuthState(), strPreKeyRecords, strKyberPreKeyRecords)) {
            storePreKeyRecords(preKeyRecords);
            storeKyberPreKeyRecords(kyberPreKeyRecords);
        }
    }


    private void initializeSecretKeys(Function<KeyMaterial, Boolean> f) {
        try {
            ECKeyPair ecKeyPair = ECKeyPair.generate();
            IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

            int registrationId = KeyHelper.generateRegistrationId(false);
            ECKeyPair signedPreKeyPair = ECKeyPair.generate();

            byte[] signature = identityKeyPair.getPrivateKey().calculateSignature(signedPreKeyPair.getPublicKey().serialize());
            ;


            //Generate one-time prekeys
            PreKeyRecord[] opk = generatePreKeys();
            this.deviceId = 1;

            SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(signedPreKeyPair, signature);
            KyberPreKeyRecords kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());

            KeyMaterial keyMaterial = new KeyMaterial(registrationId, serializeSignedPreKeys(opk), signedPreKeyRecord, identityKeyPair, signature, serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords()), base64Encoder.encodeToString(kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getKeyPair().getPublicKey().serialize()), kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getSignature(), kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId());

            if (f.apply(keyMaterial)) {
                DbHelper dbHelper = new DbHelper(getApplicationContext());
                dbHelper.clearKeyData();
                dbHelper.storeIdentityKeyPair(identityKeyPair);
                dbHelper.storeRegistrationId(registrationId, deviceId);

                signalProtocolStore = new SekretessSignalProtocolStore(getApplicationContext(), identityKeyPair, registrationId);

                storeKyberPreKeyRecords(kyberPreKeyRecords);
                storePreKeyRecords(opk);
                storeSignedPreKey(signedPreKeyRecord);
            }


        } catch (Exception e) {
            Log.i("SekretessRabbitMqService", "KeyMaterial generation failed", e);
            Toast.makeText(getApplicationContext(), "KeyMaterial generation failed " + e.getMessage(), Toast.LENGTH_LONG).show();
            sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
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


    private void storeKyberPreKeyRecords(KyberPreKeyRecords kyberPreKeyRecords) {
        DbHelper dbHelper = new DbHelper(this);

        for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords.getKyberPreKeyRecords()) {
            dbHelper.storeKyberPreKey(kyberPreKeyRecord);
            signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);
        }

        dbHelper.storeKyberPreKey(kyberPreKeyRecords.getLastResortKyberPreKeyRecord());
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecords.getLastResortKyberPreKeyRecord().getId(), kyberPreKeyRecords.getLastResortKyberPreKeyRecord());
    }

    private void storePreKeyRecords(PreKeyRecord[] preKeyRecords) {
        DbHelper dbHelper = new DbHelper(this);
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
            dbHelper.storePreKeyRecord(preKeyRecord);
        }

    }

    private void storeSignedPreKey(SignedPreKeyRecord signedPreKeyRecord) {
        DbHelper dbHelper = new DbHelper(this);
        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
        dbHelper.storeSignedPreKeyRecord(signedPreKeyRecord);
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

    public void processKeyDistributionMessage(String name, String base64Key) {
        try {
            Log.i("SignalProtocolService", "base64 keyDistributionMessage: " + base64Key);
            SenderKeyDistributionMessage senderKeyDistributionMessage = new SenderKeyDistributionMessage(Base64.getDecoder().decode(base64Key));

            new GroupSessionBuilder(signalProtocolStore).process(new SignalProtocolAddress(name, 1), senderKeyDistributionMessage);


            GroupCipher groupCipher = new GroupCipher(signalProtocolStore, new SignalProtocolAddress(name, 1));
            groupCipherTable.put(name, groupCipher);

            Log.i("SignalProtocolService", "Group chat chipper created and stored : " + name);
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error during decrypt key distribution message", e);
            Toast.makeText(getApplicationContext(), "Error during decrypt distribution message" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processAdvertisementMessage(String base64Message, String exchangeName) throws NoSessionException, InvalidMessageException, DuplicateMessageException, LegacyMessageException {
        String sender = exchangeName.split("_")[0];
        GroupCipher groupCipher = groupCipherTable.get(sender);
        Log.i("SignalProtocolService", "Decrypted advertisement exchangeName: " + exchangeName + " sender :" + sender);
        if (groupCipher != null) {
            String message = new String(groupCipher.decrypt(base64Decoder.decode(base64Message)));
            Log.i("SignalProtocolService", "Decrypted advertisement message: " + message);
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.storeDecryptedMessage(sender, message);
            broadcastNewMessageReceived();
            publishNotification(sender, message);
        } else {
            Log.i("SignalProtocolService", "No group cipher available : " + exchangeName + " sender :" + sender);
            Looper.getMainLooper();
            Handler mainHander = new Handler(Looper.getMainLooper());
            mainHander.post(() -> {
                Toast.makeText(getApplicationContext(), "No group cipher available : " + exchangeName + " sender :" + sender, Toast.LENGTH_LONG).show();
            });

        }
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) throws InvalidMessageException, InvalidVersionException, LegacyMessageException, InvalidKeyException, UntrustedIdentityException, DuplicateMessageException, InvalidKeyIdException {

        PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender, 1);
        SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, signalProtocolAddress);
        Log.i("SignalProtocolService", "" + signalProtocolStore);
        String message = new String(sessionCipher.decrypt(preKeySignalMessage, UsePqRatchet.YES));

        if (messageType == MessageType.KEY_DISTRIBUTION) {
            processKeyDistributionMessage(sender, message);
            //Store group chat info
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.storeGroupChatInfo(message, sender);
        } else {
            Log.i("SignalProtocolService", "Decrypted private message: " + message);
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.storeDecryptedMessage(sender, message);
            broadcastNewMessageReceived();
            publishNotification(sender, message);
        }
    }

    private void publishNotification(String sender, String text) {
        Intent intent = new Intent();
        var notification = new NotificationCompat.Builder(SekretessRabbitMqService.this, Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME).setContentTitle("Message from " + sender).setSilent(false).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_sekretess)).setContentText(text.substring(0, Math.min(10, text.length())).concat("...")).setSmallIcon(R.drawable.ic_notif_sekretess).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME, "New message", NotificationManager.IMPORTANCE_HIGH);
        channel.setAllowBubbles(true);
        channel.enableVibration(NotificationPreferencesUtils.getVibrationPreferences(getApplicationContext(), sender));
        boolean soundAlerts = NotificationPreferencesUtils.getSoundAlertsPreferences(getApplicationContext(), sender);
        Log.i("SekretessRabbitMqService", "soundAlerts:" + soundAlerts + "sender:" + sender);
        if (!soundAlerts) {
            notification.setSilent(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        } else {
            notification.setDefaults(0);
            notification.setSilent(false);
        }
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification.build());
        }
    }

    private void broadcastNewMessageReceived() {
        Log.i("SignalProtocolService", "Sending new-incoming-message event.Context:" + getBaseContext());
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        intent.setAction(Constants.EVENT_NEW_INCOMING_MESSAGE);
        sendBroadcast(intent);
    }


}
