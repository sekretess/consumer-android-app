package com.sekretess.utils;

import android.net.http.HttpResponseCache;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekretess.Constants;
import com.sekretess.dto.Channel;
import com.sekretess.dto.KeyMaterial;
import com.sekretess.dto.UserDto;

import net.openid.appauth.AuthState;

import java.io.OutputStream;
import java.net.HttpURLConnection;
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


    public boolean updateKeys(AuthState authState, KeyMaterial keyMaterial) {
        try {

            Future<Boolean> f = Executors.newSingleThreadExecutor()
                    .submit(() -> updateOpksInternal(authState, keyMaterial));
            return f.get();
        } catch (Exception e) {
            Log.e("KeycloakService", "Error occurred during update opks", e);
            return false;
        }
    }

    private boolean updateOpksInternal(AuthState authState, KeyMaterial keyMaterial) {
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
            userDto.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
            userDto.setPqspk(keyMaterial.getLastResortKyberPreKey());
            userDto.setPqspkSignature(encoder.encodeToString(keyMaterial.getSignature()));

            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String responseMessage = httpURLConnection.getResponseMessage();

            return httpURLConnection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                    httpURLConnection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL;

        } catch (Throwable e) {
            Log.e("KeycloakService", "Error occurred during update OPKs", e);
            return false;
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

    public boolean createUserInternal(String username, String email, String password,
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
            userDto.setPqspkSignature(base64Encoder.encodeToString(keyMaterial.getSignature()));
            userDto.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
            userDto.setPassword(password);
            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String response = httpURLConnection.getResponseMessage();
            int responseCode = httpURLConnection.getResponseCode();
            Log.i("KeycloakManager", response);
            return responseCode >= 200 && responseCode <= 299;
        } catch (Exception e) {
            Log.e("KeycloakService", "Error occurred during initialize new user", e);
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
        return false;
    }
}
