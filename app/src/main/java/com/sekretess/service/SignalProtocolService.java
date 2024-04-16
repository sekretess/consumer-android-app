package com.sekretess.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.KeyMaterial;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.dto.jwt.Payload;
import com.sekretess.model.MessageEntity;
import com.sekretess.repository.DbHelper;
import com.sekretess.ui.LoginActivity;
import com.sekretess.utils.KeycloakManager;

import org.signal.libsignal.protocol.DuplicateMessageException;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.InvalidVersionException;
import org.signal.libsignal.protocol.LegacyMessageException;
import org.signal.libsignal.protocol.SessionCipher;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.UntrustedIdentityException;
import org.signal.libsignal.protocol.ecc.Curve;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.signal.libsignal.protocol.util.Medium;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.Set;

public class SignalProtocolService extends Service {
    private SignalProtocolStore signalProtocolStore;
    private Base64.Decoder base64Decoder = Base64.getDecoder();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
    private DbHelper dbHelper;

    private BroadcastReceiver encryptedMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String encryptedMessage = intent.getStringExtra("encryptedMessage");
            String name = intent.getStringExtra("name");
            int deviceId = intent.getIntExtra("deviceId", 2);
            decryptMessage(encryptedMessage, name, deviceId);
        }
    };


    private BroadcastReceiver initializeKeyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String email = intent.getStringExtra("email");
            String username = intent.getStringExtra("username");
            String password = intent.getStringExtra("password");
            Base64.Encoder encoder = Base64.getEncoder();

            try {
                KeyMaterial keyMaterial = initializeKeys();
                boolean result = KeycloakManager.getInstance()
                        .createUser(username, email, password, keyMaterial.getRegistrationId(),
                                encoder.encodeToString(keyMaterial.getIdentityKeyPair().serialize()),
                                encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().serialize()),
                                encoder.encodeToString(keyMaterial.getSignature()),
                                keyMaterial.getOpk());
                if (result) {
                    SharedPreferences.Editor sharedPreferences =
                            getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, MODE_PRIVATE)
                                    .edit();

                    sharedPreferences.putStringSet(Constants.PREFERENCES_OPK_PROPERTY_NAME,
                            Set.of(keyMaterial.getOpk()));
                    sharedPreferences.apply();
                    sharedPreferences.commit();

                    startActivity(new Intent(SignalProtocolService.this, LoginActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "User creation failed",
                                    Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception e) {
                Log.e("SignalProtocolService", "Error occurred during initialize user", e);
                Toast.makeText(getApplicationContext(), "User creation failed",
                                Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

    private BroadcastReceiver updateOpkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            KeyMaterial keyMaterial = updateOneTimeKeys();
            SharedPreferences sharedPreferences =
                    getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, MODE_PRIVATE);

            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putStringSet(Constants.PREFERENCES_OPK_PROPERTY_NAME,
                    Set.of(keyMaterial.getOpk()));
            sharedPreferencesEditor.apply();
            sharedPreferencesEditor.commit();

            String jwtStr = sharedPreferences.getString(Constants.PREFERENCES_JWT_PROPERTY_NAME, "");

            KeycloakManager.getInstance().updateKeys(jwtStr, keyMaterial);

            Toast
                    .makeText(getApplicationContext(), "One time keys updated",
                            Toast.LENGTH_LONG)
                    .show();
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalBroadcastManager.getInstance(this).registerReceiver(encryptedMessageBroadcastReceiver,
                new IntentFilter("new-incoming-encrypted-message"));
        LocalBroadcastManager.getInstance(this).registerReceiver(initializeKeyBroadcastReceiver,
                new IntentFilter("initialize-key-event"));

        LocalBroadcastManager.getInstance(this).registerReceiver(updateOpkBroadcastReceiver,
                new IntentFilter("update-key-event"));

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, MODE_PRIVATE);
        Set<String> opkSet = sharedPreferences
                .getStringSet(Constants.PREFERENCES_OPK_PROPERTY_NAME, Set.of());
        if (opkSet.isEmpty()) {
            String jwtStr = sharedPreferences.getString(Constants.PREFERENCES_JWT_PROPERTY_NAME, "");
            initializeKeysFromJwt(Jwt.fromString(jwtStr));
        } else {
            initializePreKeys(opkSet);
        }
        dbHelper = new DbHelper(this);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void initializeKeysFromJwt(Jwt jwt) {
        try {
            Base64.Decoder base64Decoder = Base64.getDecoder();

            Payload idTokenPayload = jwt.getIdToken().getPayload();
            IdentityKeyPair identityKeyPair =
                    new IdentityKeyPair(base64Decoder.decode(idTokenPayload.getIdentityKeyPair()));
            signalProtocolStore =
                    new InMemorySignalProtocolStore(identityKeyPair,
                            idTokenPayload.getRegistrationId());

            SignedPreKeyRecord signedPreKeyRecord =
                    new SignedPreKeyRecord(base64Decoder.decode(idTokenPayload.getSignedPrekey()));
            signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
            String[] oneTimePreKeys = jwt.getIdToken().getPayload().getOneTimePrekeys();
            if (oneTimePreKeys != null) {
                for (String oneTimePreKey : oneTimePreKeys) {
                    PreKeyRecord preKeyRecord = new PreKeyRecord(base64Decoder.decode(oneTimePreKey));
                    signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
                }
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during initialize keys from JWT", e);
        }
    }

    public KeyMaterial updateOneTimeKeys() {
        //Generate one-time prekeys
        String[] opk = generateSignedPreKeys(5);
        return new KeyMaterial(opk);
    }

    public KeyMaterial initializeKeys() throws InvalidKeyException {
        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        ECKeyPair ecKeyPair = Curve.generateKeyPair();
        IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
        IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

        int registrationId = KeyHelper.generateRegistrationId(false);
        this.signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
        //Generate signed prekeyRecord
        ECKeyPair keyPair = Curve.generateKeyPair();
        byte[] signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(), keyPair.getPublicKey().serialize());

        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyId,
                System.currentTimeMillis(), keyPair, signature);

        signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

        //Generate one-time prekeys
        String[] opk = generateSignedPreKeys(15);

        return new KeyMaterial(registrationId, opk, signedPreKeyRecord, identityKeyPair, signature);
    }

    private void initializePreKeys(Set<String> prekeys) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            SecureRandom preKeyRecordIdGenerator = new SecureRandom();
            for (String prekey : prekeys) {
                int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
                PreKeyRecord preKeyRecord = new PreKeyRecord(decoder.decode(prekey));
                signalProtocolStore.storePreKey(id, preKeyRecord);
            }
        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during initialize service", e);
        }
    }

    public String[] generateSignedPreKeys(int count) {
        String[] oneTimePreKeys = new String[count];
        Base64.Encoder encoder = Base64.getEncoder();
        SecureRandom preKeyRecordIdGenerator = new SecureRandom();
        for (int i = 0; i < count; i++) {
            int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
            PreKeyRecord preKeyRecord = new PreKeyRecord(id, Curve.generateKeyPair());
            signalProtocolStore.storePreKey(id, preKeyRecord);
            oneTimePreKeys[i] = encoder.encodeToString(preKeyRecord.serialize());
        }
        return oneTimePreKeys;
    }

    public void decryptMessage(String base64Message, String name, int deviceId) {
        try {
            PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
            SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(name, deviceId);
            SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, signalProtocolAddress);
            String message = new String(sessionCipher.decrypt(preKeySignalMessage));

            ContentValues values = new ContentValues();
            values.put(MessageEntity.COLUMN_SENDER, name);
            values.put(MessageEntity.COLUMN_MESSAGE_BODY, message);
            values.put(MessageEntity.COLUMN_CREATED_AT,
                    dateTimeFormatter.format(Instant.now()));
            dbHelper.getWritableDatabase().insert(MessageEntity.TABLE_NAME,
                    null, values);

            String channelId = "sekretess_notif";
            Notification notification = new NotificationCompat
                    .Builder(SignalProtocolService.this, channelId)
                    .setContentTitle("Message from " + name)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_notif_sekretess))
                    .setContentText(message.substring(0, Math.min(10, message.length())).concat("..."))
                    .setSmallIcon(R.drawable.ic_notif_sekretess)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(getApplicationContext());
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            broadcastNewMessageReceived();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(m, notification);
            }


        } catch (Exception e) {
            Log.e("SignalProtocolService", "Error occurred during decrypt message.", e);
        }
    }

    private void broadcastNewMessageReceived() {
        Intent intent = new Intent("new-incoming-message");
        LocalBroadcastManager.getInstance(SignalProtocolService.this).sendBroadcast(intent);
    }

}
