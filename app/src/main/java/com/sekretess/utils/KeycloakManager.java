package com.sekretess.utils;

import android.util.Log;

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


    public void updateKeys(AuthState authState, KeyMaterial keyMaterial) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            Executors.newSingleThreadExecutor()
                    .submit(() -> updateOpksInternal(authState,
                            keyMaterial.getRegistrationId(),
                            encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                                    .serialize()),
                            encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                                    .getPublicKey().serialize()),
                            keyMaterial.getOpk(),
                            encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()),
                            String.valueOf(keyMaterial.getSignedPreKeyRecord().getId())));
        } catch (Exception e) {
            Log.e("KeycloakService", "Error occurred during update opks", e);
        }
    }

    private void updateOpksInternal(AuthState authState, int registrationId, String ik, String spk,
                                    String[] opk, String spkSignature, String spkId) {
        HttpURLConnection httpURLConnection = null;
        try {
            JWT jwt = new JWT(authState.getAccessToken());
            String username = jwt.getClaim("preferred_username").asString();
            URL consumerServiceUrl =
                    new URL(Constants.CONSUMER_API_URL + "/" + username + "/key-bundles");
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("PATCH");
            httpURLConnection.setRequestProperty("Authorization",
                    "Bearer " + authState.getIdToken());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = httpURLConnection.getOutputStream();

            UserDto userDto = new UserDto();
            userDto.setRegId(registrationId);
            userDto.setUsername(username);
            userDto.setIk(ik);
            userDto.setSpk(spk);
            userDto.setSpkID(spkId);
            userDto.setSPKSignature(spkSignature);
            userDto.setOpk(opk);

            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String responseMessage = httpURLConnection.getResponseMessage();

        } catch (Throwable e) {
            Log.e("KeycloakService", "Error occurred during update OPKs", e);
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    public boolean createUser(String username, String email, String password, int registrationId,
                              String ik, String spk, String[] opk, String spkSignature, String spkId) {
        try {
            Future<Boolean> future =
                    Executors
                            .newSingleThreadExecutor()
                            .submit(() -> createUserInternal(username, email, password,
                                    registrationId, ik, spk, opk, spkSignature, spkId));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean createUserInternal(String username, String email, String password,
                                      int registrationId, String ik, String spk, String[] opk,
                                      String spkSignature, String spkId) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL consumerServiceUrl = new URL(Constants.CONSUMER_API_URL);
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = httpURLConnection.getOutputStream();
//
            UserDto userDto = new UserDto();
            userDto.setRegId(registrationId);
            userDto.setUsername(username);
            userDto.setEmail(email);
            userDto.setIk(ik);
            userDto.setSpk(spk);
            userDto.setSpkID(spkId);
            userDto.setSPKSignature(spkSignature);
            userDto.setOpk(opk);
            userDto.setPassword(password);
            Channel[] channels = new Channel[1];
            Channel channel = new Channel();
            channel.setName("email");
            channel.setValue("12313");
            channels[0] = channel;
            userDto.setChannels(channels);
            String jsonObject = objectMapper.writeValueAsString(userDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String response = httpURLConnection.getResponseMessage();
            int responseCode = httpURLConnection.getResponseCode();
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
