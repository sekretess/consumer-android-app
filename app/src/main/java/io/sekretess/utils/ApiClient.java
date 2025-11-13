package io.sekretess.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.signal.libsignal.protocol.state.PreKeyRecord;

import io.sekretess.BuildConfig;
import io.sekretess.SekretessApplication;
import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.dto.BusinessDto;
import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.KyberPreKeyRecords;
import io.sekretess.dto.OneTimeKeyBundleDto;
import io.sekretess.dto.RefreshTokenRequestDto;
import io.sekretess.dto.UserDto;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.repository.DbHelper;
import io.sekretess.service.AuthService;
import io.sekretess.ui.LoginActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.login.LoginException;

public class ApiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static ExecutorService networkExecutors = Executors.newFixedThreadPool(3);
    private final AuthService authService;
    private final SekretessApplication application;

    public ApiClient(AuthService authService, SekretessApplication application) {
        this.authService = authService;
        this.application = application;
    }


    public boolean deleteUser() {
        try {
            Future<Boolean> future = networkExecutors
                    .submit(this::deleteUserInternal);
            return future.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast("Error occurred: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteUserInternal() {
        Request request = new Request.Builder().delete().url(BuildConfig.CONSUMER_API_URL).build();
        OkHttpClient httpClient = authorizedHttpClient();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast("User delete failed." + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast("User deleted.");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during delete consumer ", e);
            showToast("Error occurred during delete consumer " + e.getMessage());
            return false;
        }
    }

    public boolean subscribeToBusiness(String business) {
        try {
            Future<Boolean> future = networkExecutors.submit(() -> subscribeToBusinessInternal(business));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast("Error occurred : " + e.getMessage());
            return false;
        }
    }

    private boolean subscribeToBusinessInternal(String business) {
        OkHttpClient httpClient = authorizedHttpClient();
        Request request = new Request
                .Builder()
                .url(BuildConfig.CONSUMER_API_URL + "/ads/businesses/" + business)
                .post(Util.EMPTY_REQUEST).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast("Subscription failed: " + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast("Successfully subscribed:");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occur red during subscribe to " + business, e);
            showToast("Error occurred: " + e.getMessage());
            return false;
        }
    }

    public boolean unSubscribeFromBusiness(String business) {
        try {
            Future<Boolean> future = networkExecutors
                    .submit(() -> unSubscribeFromBusinessInternal(business));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait unsubscribe from business thread", e);
            showToast("Unsubscription failed: " + e.getMessage());
            return false;
        }
    }

    private boolean unSubscribeFromBusinessInternal(String business) {
        OkHttpClient httpClient = authorizedHttpClient();
        Request request = new Request.Builder()
                .url(BuildConfig.CONSUMER_API_URL + "/ads/businesses/" + business)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast("Unsubscribe failed: " + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast("Successfully unsubscribed");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during unsubscribe from " + business, e);
            showToast("Unsubscribe failed: " + e.getMessage());
            return false;
        }
    }

    public List<BusinessDto> getBusinesses() {
        try {
            Future<List<BusinessDto>> future = networkExecutors
                    .submit(() -> getBusinessesInternal());
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses.", e);
            showToast("Get businesses failed: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private List<BusinessDto> getBusinessesInternal() {
        OkHttpClient httpClient = authorizedHttpClient();
        Request request = new Request.Builder().url(BuildConfig.BUSINESS_API_URL)
                .get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("ApiClient", "HTTP error: " + response.code() + " URL: "
                        + BuildConfig.BUSINESS_API_URL + " errorMessage: " + response.message());
                showToast("Can not get list of businesses: " + response.code()
                        + "\n" + response.message());
                return Collections.EMPTY_LIST;
            } else {
                List<BusinessDto> businessDtos = objectMapper.readValue(response.body().string(),
                        new TypeReference<List<BusinessDto>>() {
                        });
                return businessDtos;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses {}", e);
            showToast("Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public List<String> getSubscribedBusinesses() {
        try {
            Future<List<String>> future = networkExecutors
                    .submit(() -> getSubscribedBusinessesInternal());
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get subscribed businesses.", e);
            showToast("Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private List<String> getSubscribedBusinessesInternal() {
        OkHttpClient httpClient = authorizedHttpClient();
        Request request = new Request.Builder().url(BuildConfig.CONSUMER_API_URL + "/ads/businesses")
                .get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("ApiClient", "HTTP error " + response.code() + " message: "
                        + response.message());
                showToast("Can not get subscribed businesses. " + response.code() + "\n"
                        + response.message());
                return Collections.EMPTY_LIST;
            } else {
                List result = objectMapper.readValue(response.body().string(), List.class);
                if (result == null) return Collections.EMPTY_LIST;
                return result;
            }

        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses", e);
            showToast("Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }


    public boolean upsertKeyStore(KeyMaterial keyMaterial) throws Exception {
        return networkExecutors.submit(() -> internalUpsertKeyStore(keyMaterial)).get();
    }

    private boolean internalUpsertKeyStore(KeyMaterial keyMaterial) {
        OkHttpClient httpClient = authorizedHttpClient();
        try {

            KeyBundleDto keyBundleDto = Mappers.toKeyBundleDto(keyMaterial);
            String json = objectMapper.writeValueAsString(keyBundleDto);

            Request request = new Request.Builder()
                    .url(new URL(BuildConfig.CONSUMER_API_URL + "/keystores"))
                    .put(RequestBody.create(MediaType.parse("application/json"), json))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.i("ApiClient", "Upsert success");
                    showToast("One time keys updated");
                    return true;
                } else {
                    Log.e("ApiClient", "Upsert failed" + response.code() + ": "
                            + response.message());
                    showToast("One time keys update failed with response code " +
                            response.code() + " ResponseMessage " + response.message());
                    return false;
                }
            }

        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during upsert keystore", e);
            showToast("One time keys update failed : " + e.getMessage());
            return false;
        }

    }

    public AuthResponse login(String username, String password) throws Exception {
        return networkExecutors.submit(() -> loginInternal(username, password)).get();
    }

    private AuthResponse loginInternal(String username, String password) throws Exception {
        AuthRequest authRequest = new AuthRequest(username, password);
        String jsonObject = objectMapper.writeValueAsString(authRequest);
        OkHttpClient httpClient = anonymousHttpClient();

        Request request = new Request
                .Builder()
                .url(new URL(BuildConfig.CONSUMER_API_URL + "/auth/login"))
                .post(RequestBody
                        .create(MediaType.parse("application/json"), jsonObject))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), AuthResponse.class);
            } else {
                Log.e("ApiClient", "HTTP error: " + response.code());
                throw new LoginException("Login failed." + response.message());
            }
        }
    }

    public boolean updateOneTimeKeys(PreKeyRecord[] preKeyRecords, KyberPreKeyRecords kyberPreKeyRecords) {
        try {
            String[] strPreKeyRecords = SerializationUtils.serializeSignedPreKeys(preKeyRecords);
            String[] strKyberPreKeyRecords = SerializationUtils.serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords());

            return networkExecutors.submit(() -> updateOpksInternal(strPreKeyRecords,
                            strKyberPreKeyRecords))
                    .get();
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred while updateOneTimeKeys", e);
            return false;
        }
    }

    private boolean updateOpksInternal(String[] preKeyRecords, String[] kyberPreKeyRecords) {
        try {
            OneTimeKeyBundleDto oneTimeKeyBundleDto = new OneTimeKeyBundleDto(preKeyRecords, kyberPreKeyRecords);

            String jsonObject = objectMapper.writeValueAsString(oneTimeKeyBundleDto);
            OkHttpClient httpClient = authorizedHttpClient();
            Request request = new Request
                    .Builder()
                    .post(RequestBody
                            .create(MediaType.parse("application/json"), jsonObject))
                    .url(new URL(BuildConfig.CONSUMER_API_URL + "/onetimekeystores")).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    showToast("One time keys updated");
                    return true;
                } else {
                    showToast("One time keys update failed with response code " +
                            response.code() + " ResponseMessage " + response.message());
                    return false;
                }
            }


        } catch (Exception e) {
            Log.i("ApiClient", "Exception occurred during update keys", e);
            showToast("Exception occurred during update keys " + e.getMessage());
            return false;
        }
    }

    public Optional<AuthResponse> refresh(RefreshTokenRequestDto refreshTokenRequestDto) {
        try {
            Future<Optional<AuthResponse>> future = networkExecutors.submit(() -> refreshInternal(refreshTokenRequestDto));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during refresh", e);
            showToast("Error occurred during refresh: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AuthResponse> refreshInternal(RefreshTokenRequestDto refreshTokenRequestDto) throws JsonProcessingException, MalformedURLException {
        OkHttpClient httpClient = anonymousHttpClient();
        String jsonObject = objectMapper.writeValueAsString(refreshTokenRequestDto);
        Request request = new Request
                .Builder()
                .url(new URL(BuildConfig.CONSUMER_API_URL + "/auth/refresh"))
                .post(RequestBody.create(MediaType.parse("application/json"), jsonObject))
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast("JWT refresh failed. " + response.code() + "\n" + response.message());
                return Optional.empty();
            }

            AuthResponse authResponse = objectMapper.readValue(response.body().string(), AuthResponse.class);
            return Optional.of(authResponse);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public boolean createUser(String username, String email, String password) {
        try {
            Future<Boolean> future = networkExecutors
                    .submit(() -> createUserInternal(username, email, password));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during signup", e);
            showToast("Error occurred during signup: " + e.getMessage());
            return false;
        }
    }

    private boolean createUserInternal(String username, String email, String password) {
        OkHttpClient httpClient = anonymousHttpClient();
        try {
            UserDto userDto = new UserDto();
            userDto.setUsername(username);
            userDto.setEmail(email);
            userDto.setPassword(password);
            String jsonObject = objectMapper.writeValueAsString(userDto);

            Request request = new Request.Builder().url(new URL(BuildConfig.CONSUMER_API_URL))
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonObject))
                    .build();
            largeLog("ApiClient", jsonObject);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    showToast("User creation failed. " + response.code() + "\n" + response.message());
                    return false;
                } else {
                    showToast("User created. " + response.code() + "\n" + response.message());
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during initialize new user {0}", e);
            showToast("Error occurred during initialize new user: " + e.getMessage());
        }
        return false;
    }

    private void largeLog(String tag, String content) {
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

    private void showToast(String text) {
        ContextCompat.getMainExecutor(application.getApplicationContext())
                .execute(() -> Toast
                        .makeText(application.getApplicationContext(), text, Toast.LENGTH_LONG)
                        .show());
    }

    private OkHttpClient authorizedHttpClient() {
        try {
            String accessToken = authService.getAccessToken().toString();

            return new OkHttpClient.Builder()
                    .addInterceptor(new SekretessHttpInterceptor(accessToken))
                    .authenticator(new BearerAuthenticator(application))
                    .build();
        } catch (Exception e) {
            ContextCompat.startActivity(application.getApplicationContext(),
                    new Intent(application.getApplicationContext(), LoginActivity.class), null);
            return null;
        }
    }

    private OkHttpClient anonymousHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    public void logout() {


    }
}
