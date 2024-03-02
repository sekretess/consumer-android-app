package com.sekretess.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sekretess.R;
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

public class SignupActivity extends AppCompatActivity {

    private SignalProtocolStore signalProtocolStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Button btnSignup = (Button) findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(this::initializeNewUser);
    }

    private void initializeNewUser(View view) {
        try {
            int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
            ECKeyPair ecKeyPair = Curve.generateKeyPair();
            IdentityKey identityKey = new IdentityKey(ecKeyPair.getPublicKey());
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKey, ecKeyPair.getPrivateKey());

            int registrationId = KeyHelper.generateRegistrationId(false);
            this.signalProtocolStore = new SekretessProtocolStore(identityKeyPair, registrationId);
//
            SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(identityKeyPair, signedPreKeyId);
            signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
        } catch (Exception e) {

        }
    }

    private void createUser() throws IOException {
        URL consumerServiceUrl = new URL("http://78.47.90.202:8081/api/v1/consumers");
        HttpURLConnection httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
        httpURLConnection.setRequestMethod("POST");
        OutputStream outputStream = httpURLConnection.getOutputStream();
        UserDto userDto = new UserDto();

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