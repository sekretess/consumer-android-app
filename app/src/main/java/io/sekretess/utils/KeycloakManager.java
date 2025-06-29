package io.sekretess.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sekretess.Constants;
import io.sekretess.MainActivity;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.UserDto;

import net.openid.appauth.AuthState;

import org.signal.libsignal.protocol.InvalidKeyException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KeycloakManager {

    private static KeycloakManager instance;


    private final ObjectMapper objectMapper = new ObjectMapper();

    private KeycloakManager() {

    }

    public synchronized static KeycloakManager getInstance() {
        if (instance == null) {
            instance = new KeycloakManager();
        }
        return instance;
    }


    public void updateKeys(Context context, AuthState authState, KeyMaterial keyMaterial) {
        Executors.newSingleThreadExecutor()
                .submit(() -> updateOpksInternal(context, authState, keyMaterial));
    }

    private void updateOpksInternal(Context context, AuthState authState, KeyMaterial keyMaterial) {
        HttpURLConnection httpURLConnection = null;
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            JWT jwt = new JWT(authState.getAccessToken());
            String username = jwt.getClaim(Constants.USERNAME_CLAIM).asString();
            URL consumerServiceUrl =
                    new URL(Constants.CONSUMER_API_URL + "/key-bundles");
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("PATCH");
            httpURLConnection.setRequestProperty("Authorization",
                    "Bearer " + authState.getIdToken());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = httpURLConnection.getOutputStream();

            UserDto userDto = new UserDto();
            userDto.setRegId(keyMaterial.getRegistrationId());
            userDto.setUsername(username);
            userDto.setIk(encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                    .serialize()));
            userDto.setSpk(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                    .getPublicKey().serialize()));
            userDto.setSpkID(String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));
            userDto.setSPKSignature(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()));
            userDto.setOpk(keyMaterial.getOpk());
            // Setting PostQuantum keys
            userDto.setOpqk(keyMaterial.getSerializedKyberPreKeys());
            userDto.setPqspk(keyMaterial.getLastResortKyberPreKey());
            userDto.setPqspkSignature(encoder.encodeToString(keyMaterial.getLastResortKeyberPreKeySignature()));
            userDto.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));


            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String responseMessage = httpURLConnection.getResponseMessage();

            boolean isSuccess = httpURLConnection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                    httpURLConnection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL;
            if (isSuccess) {
                Toast.makeText(context, "One time keys updated", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "One time keys update failed with response code "
                                + httpURLConnection.getResponseCode() + " ResponseMessage " + responseMessage,
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.i("KeycloakManager", "Exception occurred during update keys", e);
            Toast.makeText(context, "Exception occurred during update keys " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    public boolean createUser(String username, String email, String password, KeyMaterial keyMaterial) {

        try {
            Future<Boolean> future =
                    Executors
                            .newSingleThreadExecutor()
                            .submit(() -> createUserInternal(username, email, password, keyMaterial));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("KeycloakManager", "Error occurred during signup", e);
            return false;
        }
    }

    private boolean createUserInternal(String username, String email, String password,
                                       KeyMaterial keyMaterial) {
        HttpURLConnection httpURLConnection = null;
        Base64.Encoder base64Encoder = Base64.getEncoder();
        try {
            URL consumerServiceUrl = new URL(Constants.CONSUMER_API_URL);
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = httpURLConnection.getOutputStream();
//


            UserDto userDto = new UserDto();
            userDto.setRegId(keyMaterial.getRegistrationId());
            userDto.setUsername(username);
            userDto.setEmail(email);
            userDto.setIk(base64Encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                    .serialize()));
            userDto.setSpk(base64Encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                    .getPublicKey().serialize()));
            userDto.setSpkID(String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));
            userDto.setSPKSignature(base64Encoder.encodeToString(keyMaterial.getSignature()));
            userDto.setOpk(keyMaterial.getOpk());
            // Setting PostQuantum keys
            userDto.setOpqk(keyMaterial.getSerializedKyberPreKeys());
            userDto.setPqspk(keyMaterial.getLastResortKyberPreKey());
            userDto.setPqspkSignature(base64Encoder.encodeToString(keyMaterial.getLastResortKeyberPreKeySignature()));
            userDto.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
            userDto.setPassword(password);
            String jsonObject = objectMapper.writeValueAsString(userDto);

            largeLog("KeycloakService", jsonObject);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            int responseCode = httpURLConnection.getResponseCode();
            return responseCode >= 200 && responseCode <= 299;
        } catch (Exception e) {

            Log.e("KeycloakService", "Error occurred during initialize new user {0}", e);
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
        return false;
    }

    private static void largeLog(String tag, String content) {
        final int SEG_LENGTH = 4000;
        do {
            if (content.length() <= SEG_LENGTH) {
                Log.d(tag, content);
                break;
            }
            Log.d(tag, content.substring(0, SEG_LENGTH));
            content = content.substring(SEG_LENGTH);
        } while (!content.isEmpty());
    }
}
