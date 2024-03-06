package com.sekretess.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekretess.R;
import com.sekretess.dto.Channel;
import com.sekretess.dto.UserDto;
import com.sekretess.store.SekretessProtocolStore;

import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.ecc.Curve;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.signal.libsignal.protocol.util.Medium;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private SignalProtocolStore signalProtocolStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Button btnSignup = (Button) findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(this::initializeNewUser);
    }

    private void initializeNewUser(View view) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();

            int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
            ECKeyPair ecKeyPair = Curve.generateKeyPair();
            IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

            int registrationId = KeyHelper.generateRegistrationId(false);
            this.signalProtocolStore = new SekretessProtocolStore(identityKeyPair, registrationId);
//
            SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(identityKeyPair, signedPreKeyId);
            signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

            String[] opk = generateSignedPreKeys(15);

            Executors.newSingleThreadExecutor().submit(() -> createUser(encoder.encodeToString(identityKeyPair.serialize()),
                    encoder.encodeToString(signedPreKeyRecord.serialize()), opk));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUser(String ik, String spk, String[] opk) {

        try {
            String email = ((CharSequence) ((EditText) findViewById(R.id.txtSignupEmail)).getText()).toString();
            String password = ((CharSequence) ((EditText) findViewById(R.id.txtSignupPassword)).getText()).toString();

            URL consumerServiceUrl = new URL("http://78.47.90.202:8081/api/v1/consumers");
            HttpURLConnection httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
//
            UserDto userDto = new UserDto();
            userDto.setUsername(email.split("@")[0]);
            userDto.setEmail(email);
            userDto.setIpk(ik);
            userDto.setSpk(spk);
            userDto.setOpk(opk);
            userDto.setSigPrekey("123");
            userDto.setPassword(password);
            Channel[] channels = new Channel[1];
            Channel channel = new Channel();
            channel.setName("email");
            channel.setValue("12313");
            channels[0] = channel;
            userDto.setChannels(channels);
            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes("UTF-8"));
            outputStream.flush();
            httpURLConnection.disconnect();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private SignedPreKeyRecord generateSignedPreKey(IdentityKeyPair identityKeyPair, int signedPreKeyId) throws InvalidKeyException {
        ECKeyPair keyPair = Curve.generateKeyPair();
        byte[] signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(), keyPair.getPublicKey().serialize());
        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);


    }

    private String[] generateSignedPreKeys(int count) {
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

}