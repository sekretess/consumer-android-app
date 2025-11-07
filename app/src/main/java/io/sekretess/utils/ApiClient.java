package io.sekretess.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.openid.appauth.AuthState;

import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;

import io.sekretess.BuildConfig;
import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.KyberPreKeyRecords;
import io.sekretess.dto.OneTimeKeyBundleDto;
import io.sekretess.dto.UserDto;
import io.sekretess.repository.DbHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    public static boolean deleteUser(Context context, AuthState authState) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> deleteUserInternal(context, authState));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast(context, "Error occurred: " + e.getMessage());
            return false;
        }
    }

    private static boolean deleteUserInternal(Context context, AuthState authState) {
        Request request = new Request.Builder().delete().url(BuildConfig.CONSUMER_API_URL).build();
        OkHttpClient httpClient = httpClient(authState, context);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast(context, "User delete failed." + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast(context, "User deleted.");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during delete consumer ", e);
            showToast(context, "Error occurred during delete consumer " + e.getMessage());
            return false;
        }
    }

    public static boolean subscribeToBusiness(Context context, String business) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> subscribeToBusinessInternal(context, business));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast(context, "Error occurred : " + e.getMessage());
            return false;
        }
    }

    private static boolean subscribeToBusinessInternal(Context context, String business) {
        AuthState authState = authState(context);
        OkHttpClient httpClient = httpClient(authState, context);
        Request request = new Request.Builder().url(BuildConfig.CONSUMER_API_URL + "/ads/businesses/" + business).post(Util.EMPTY_REQUEST).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast(context, "Subscription failed: " + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast(context, "Successfully subscribed:");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occur red during subscribe to " + business, e);
            showToast(context, "Error occurred: " + e.getMessage());
            return false;
        }
    }

    public static boolean unSubscribeFromBusiness(Context context, String business) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> unSubscribeFromBusinessInternal(context, business));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait unsubscribe from business thread", e);
            showToast(context, "Unsubscription failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean unSubscribeFromBusinessInternal(Context context, String business) {
        AuthState authState = authState(context);
        OkHttpClient httpClient = httpClient(authState, context);
        Request request = new Request.Builder().url(BuildConfig.CONSUMER_API_URL + "/ads/businesses/" + business).delete().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                showToast(context, "Unsubscribe failed: " + response.code() + "\n" + response.message());
                return false;
            } else {
                showToast(context, "Successfully unsubscribed");
                return true;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during unsubscribe from " + business, e);
            showToast(context, "Unsubscribe failed: " + e.getMessage());
            return false;
        }
    }

    public static List<BusinessDto> getBusinesses(Context context) {
        try {
            Future<List<BusinessDto>> future = Executors.newSingleThreadExecutor().submit(() -> getBusinessesInternal(context));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses.", e);
            showToast(context, "Get businesses failed: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private static List<BusinessDto> getBusinessesInternal(Context context) {
        AuthState authState = authState(context);
        OkHttpClient httpClient = httpClient(authState, context);
        Request request = new Request.Builder().url(BuildConfig.BUSINESS_API_URL)
                .get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("ApiClient", "HTTP error: " + response.code() + " URL: " + BuildConfig.BUSINESS_API_URL + " errorMessage: " + response.message());
                showToast(context, "Can not get list of businesses: " + response.code() + "\n" + response.message());
                return Collections.EMPTY_LIST;
            } else {
                List<BusinessDto> businessDtos = objectMapper.readValue(response.body().string(), new TypeReference<List<BusinessDto>>() {
                });
                return businessDtos;
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses {}", e);
            showToast(context, "Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public static List<String> getSubscribedBusinesses(Context context) {
        try {
            Future<List<String>> future = Executors.newSingleThreadExecutor().submit(() -> getSubscribedBusinessesInternal(context));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get subscribed businesses.", e);
            showToast(context, "Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private static List<String> getSubscribedBusinessesInternal(Context context) {
        AuthState authState = authState(context);
        OkHttpClient httpClient = httpClient(authState, context);
        Request request = new Request.Builder().url(BuildConfig.CONSUMER_API_URL + "/ads/businesses")
                .get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("ApiClient", "HTTP error " + response.code() + " message: " + response.message());
                showToast(context, "Can not get subscribed businesses. " + response.code() + "\n" + response.message());
                return Collections.EMPTY_LIST;
            } else {
                List result = objectMapper.readValue(response.body().string(), List.class);
                if (result == null) return Collections.EMPTY_LIST;
                return result;
            }

        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses", e);
            showToast(context, "Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }


    public static boolean upsertKeyStore(Context context, KeyMaterial keyMaterial) throws Exception {
        return Executors.newSingleThreadExecutor().submit(() -> internalUpsertKeyStore(context, keyMaterial)).get();
    }

    private static boolean internalUpsertKeyStore(Context context, KeyMaterial keyMaterial) {
        AuthState authState = authState(context);
        OkHttpClient httpClient = httpClient(authState, context);
        try {

            KeyBundleDto keyBundleDto = Mappers.toKeyBundleDto(keyMaterial);
            String json = objectMapper.writeValueAsString(keyBundleDto);

            Request request = new Request.Builder().url(new URL(BuildConfig.CONSUMER_API_URL + "/keystores"))
                    .put(RequestBody.create(MediaType.parse("application/json"), json)).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.i("ApiClient", "Upsert success");
                    showToast(context, "One time keys updated");
                    return true;
                } else {
                    Log.e("ApiClient", "Upsert failed" + response.code() + ": " + response.message());
                    showToast(context, "One time keys update failed with response code " + response.code() + " ResponseMessage " + response.message());
                    return false;
                }
            }

        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during upsert keystore", e);
            showToast(context, "One time keys update failed : " + e.getMessage());
            return false;
        }

    }

    public static boolean updateOneTimeKeys(Context context, PreKeyRecord[] preKeyRecords, KyberPreKeyRecords kyberPreKeyRecords) {
        try {
            String[] strPreKeyRecords = SerializationUtils.serializeSignedPreKeys(preKeyRecords);
            String[] strKyberPreKeyRecords = SerializationUtils.serializeKyberPreKeys(kyberPreKeyRecords.getKyberPreKeyRecords());

            return Executors.newSingleThreadExecutor().submit(() -> updateOpksInternal(context, authState,
                    strPreKeyRecords, strKyberPreKeyRecords)).get();
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred while updateOneTimeKeys", e);
            return false;
        }
    }

    private static boolean updateOpksInternal(Context context, AuthState authState, String[] preKeyRecords, String[] kyberPreKeyRecords) {
        try {
            OneTimeKeyBundleDto oneTimeKeyBundleDto = new OneTimeKeyBundleDto(preKeyRecords, kyberPreKeyRecords);

            String jsonObject = objectMapper.writeValueAsString(oneTimeKeyBundleDto);
            OkHttpClient httpClient = httpClient(authState, context);
            Request request = new Request.Builder().post(RequestBody.create(MediaType.parse("application/json"), jsonObject))
                    .url(new URL(BuildConfig.CONSUMER_API_URL + "/onetimekeystores")).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    showToast(context, "One time keys updated");
                    return true;
                } else {
                    showToast(context, "One time keys update failed with response code " + response.code() + " ResponseMessage " + response.message());
                    return false;
                }
            }


        } catch (Exception e) {
            Log.i("ApiClient", "Exception occurred during update keys", e);
            showToast(context, "Exception occurred during update keys " + e.getMessage());
            return false;
        }
    }

    public static boolean createUser(Context context, String username, String email, String password) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> createUserInternal(context, username, email, password));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during signup", e);
            showToast(context, "Error occurred during signup: " + e.getMessage());
            return false;
        }
    }

    private static boolean createUserInternal(Context context, String username, String email, String password) {
        OkHttpClient httpClient = httpClient(authState(context), context);
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
                    showToast(context, "User creation failed. " + response.code() + "\n" + response.message());
                    return false;
                } else {
                    showToast(context, "User created. " + response.code() + "\n" + response.message());
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during initialize new user {0}", e);
            showToast(context, "Error occurred during initialize new user: " + e.getMessage());
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

    private static void showToast(Context context, String text) {
        ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }

    private static OkHttpClient httpClient(AuthState authState, Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(new SekretessHttpInterceptor(authState.getIdToken()))
                .authenticator(new BearerAuthenticator(authState, context)).build();
    }

    private static AuthState authState(Context context) {
        try (DbHelper dbHelper = new DbHelper(context)) {
            return dbHelper.getAuthState();
        }
    }
}
