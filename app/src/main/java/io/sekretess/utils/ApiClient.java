package io.sekretess.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.openid.appauth.AuthState;

import io.sekretess.Constants;
import io.sekretess.dto.BusinessDto;
import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.OneTimeKeyBundleDto;
import io.sekretess.dto.UserDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static boolean deleteUser(Context context, String jwt) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> deleteUserInternal(context, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast(context, "Error occurred: " + e.getMessage());
            return false;
        }
    }

    private static boolean deleteUserInternal(Context context, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL);
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            Log.i("ApiClient", "Delete consumer API " + urlConnection.getResponseCode());
            boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
            if (!isSuccess) {
                showToast(context, "User delete failed." + urlConnection.getResponseCode() + "\n" +
                        urlConnection.getResponseMessage());
            }
            return isSuccess;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during delete consumer ", e);
            showToast(context, "Error occurred during delete consumer " + e.getMessage());
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }


    }

    public static boolean subscribeToBusiness(Context context, String business, String jwt) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> subscribeToBusinessInternal(context, business, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            showToast(context, "Error occurred : " + e.getMessage());
            return false;
        }
    }

    private static boolean subscribeToBusinessInternal(Context context, String business, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses/" + business);
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            Log.i("ApiClient", "Subscribe to business resultcode " + urlConnection.getResponseCode());
            boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
            if (!isSuccess) {
                showToast(context, "Subscription failed: " + urlConnection.getResponseCode() + "\n" +
                        urlConnection.getResponseMessage());
            }
            return isSuccess;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occur red during subscribe to " + business, e);
            showToast(context, "Error occurred: " + e.getMessage());
            return false;
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public static boolean unSubscribeFromBusiness(Context context, String business, String jwt) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> unSubscribeFromBusinessInternal(context, business, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait unsubscribe from business thread", e);
            showToast(context, "Unsubscription failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean unSubscribeFromBusinessInternal(Context context, String business, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses/" + business);
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
            if (!isSuccess) {
                showToast(context, "Unsubscribe failed: " + urlConnection.getResponseCode() + "\n" +
                        urlConnection.getResponseMessage());
            }
            return isSuccess;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during unsubscribe from " + business, e);
            showToast(context, "Unsubscribe failed: " + e.getMessage());
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static List<BusinessDto> getBusinesses(Context context) {
        try {
            Future<List<BusinessDto>> future = Executors.newSingleThreadExecutor()
                    .submit(() -> getBusinessesInternal(context));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses.", e);
            showToast(context, "Get businesses failed: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private static List<BusinessDto> getBusinessesInternal(Context context) {
        HttpURLConnection urlConnection = null;
        try {
            URL businessApiUrl = new URL(Constants.BUSINESS_API_URL);
            urlConnection = (HttpURLConnection) businessApiUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
            if (!isSuccess) {
                Log.e("ApiClient", "HTTP error " + urlConnection.getResponseCode() + " URL: " + Constants.BUSINESS_API_URL);
                showToast(context, "Can not get list of businesses: " + urlConnection.getResponseCode() + "\n"
                        + urlConnection.getResponseMessage());
                return Collections.EMPTY_LIST;
            }

            StringBuilder response = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            List<BusinessDto> businessDtos = objectMapper.readValue(response.toString(), new TypeReference<List<BusinessDto>>() {
            });
            return businessDtos;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses {}", e);
            showToast(context, "Error occurred: " + e.getMessage());
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return Collections.EMPTY_LIST;
    }

    public static List<String> getSubscribedBusinesses(Context context, String jwt) {
        try {
            Future<List<String>> future = Executors.newSingleThreadExecutor()
                    .submit(() -> getSubscribedBusinessesInternal(context, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get subscribed businesses.", e);
            showToast(context, "Error occurred: " + e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    private static List<String> getSubscribedBusinessesInternal(Context context, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL businessApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses");
            urlConnection = (HttpURLConnection) businessApiUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
            if (!isSuccess) {
                Log.e("ApiClient", "HTTP error " + urlConnection.getResponseCode() + " URL: " + urlConnection.getURL().toString());
                showToast(context, "Can not get subscribed businesses. " + urlConnection.getResponseCode() + "\n"
                        + urlConnection.getResponseMessage());
                return Collections.EMPTY_LIST;
            }

            StringBuilder response = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            List result = objectMapper.readValue(response.toString(), List.class);
            if (result == null) return Collections.EMPTY_LIST;
            return result;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses", e);
            showToast(context, "Error occurred: " + e.getMessage());
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return Collections.EMPTY_LIST;
    }


    public static void upsertKeyStore(Context context, KeyMaterial keyMaterial, String jwt) {
        Executors.newSingleThreadExecutor().submit(() -> {
            HttpURLConnection urlConnection = null;
            try {

                URL businessApiUrl = new URL(Constants.CONSUMER_API_URL + "/keystores");
                urlConnection = (HttpURLConnection) businessApiUrl.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                OutputStream outputStream = urlConnection.getOutputStream();
                KeyBundleDto keyBundleDto = Mappers.toKeyBundleDto(keyMaterial);
                String json = objectMapper.writeValueAsString(keyBundleDto);
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
                String responseMessage = urlConnection.getResponseMessage();
                boolean isSuccess = isSuccessResponse(urlConnection.getResponseCode());
                if (isSuccess) {
                    Log.i("ApiClient", "Upsert success");
                    showToast(context, "One time keys updated");
                } else {
                    Log.e("ApiClient", "Upsert failed" + urlConnection.getResponseCode()
                            + ": " + urlConnection.getResponseMessage());
                    showToast(context, "One time keys update failed with response code "
                            + urlConnection.getResponseCode()
                            + " ResponseMessage " + responseMessage);

                }
            } catch (Exception e) {
                Log.e("ApiClient", "Error occurred during upsert keystore", e);
                showToast(context, "One time keys update failed : " + e.getMessage());

            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        });
    }


    public static void updateOneTimeKeys(Context context, AuthState authState, String[] preKeyRecords,
                                         String[] kyberPreKeyRecords) {
        Executors.newSingleThreadExecutor().submit(() -> updateOpksInternal(context, authState,
                preKeyRecords, kyberPreKeyRecords));
    }

    private static void updateOpksInternal(Context context, AuthState authState, String[] preKeyRecords,
                                           String[] kyberPreKeyRecords) {
        HttpURLConnection httpURLConnection = null;

        try {
            URL consumerServiceUrl = new URL(Constants.CONSUMER_API_URL + "/onetimekeystores");
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + authState.getIdToken());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = httpURLConnection.getOutputStream();

            OneTimeKeyBundleDto oneTimeKeyBundleDto = new OneTimeKeyBundleDto(preKeyRecords,
                    kyberPreKeyRecords);

            String jsonObject = objectMapper.writeValueAsString(oneTimeKeyBundleDto);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            String responseMessage = httpURLConnection.getResponseMessage();

            boolean isSuccess = isSuccessResponse(httpURLConnection.getResponseCode());
            if (isSuccess) {
                showToast(context, "One time keys updated");
            } else {
                showToast(context, "One time keys update failed with response code "
                        + httpURLConnection.getResponseCode()
                        + " ResponseMessage " + responseMessage);
            }

        } catch (Exception e) {
            Log.i("ApiClient", "Exception occurred during update keys", e);
            showToast(context, "Exception occurred during update keys " + e.getMessage());
        } finally {
            if (httpURLConnection != null) httpURLConnection.disconnect();
        }
    }

    public static boolean createUser(Context context, String username, String email, String password, KeyMaterial keyMaterial) {

        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> createUserInternal(context, username, email, password, keyMaterial));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during signup", e);
            showToast(context, "Error occurred during signup: " + e.getMessage());
            return false;
        }
    }

    private static boolean createUserInternal(Context context, String username, String email, String password,
                                              KeyMaterial keyMaterial) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL consumerServiceUrl = new URL(Constants.CONSUMER_API_URL);
            httpURLConnection = (HttpURLConnection) consumerServiceUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = httpURLConnection.getOutputStream();
//

            UserDto userDto = Mappers.toUserDto(keyMaterial);
            userDto.setUsername(username);
            userDto.setEmail(email);
            userDto.setPassword(password);

            String jsonObject = objectMapper.writeValueAsString(userDto);

            largeLog("ApiClient", jsonObject);
            outputStream.write(jsonObject.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            boolean isSuccess = isSuccessResponse(httpURLConnection.getResponseCode());
            if (!isSuccess) {
                showToast(context, "User creation failed. " + httpURLConnection.getResponseCode() +
                        "\n" + httpURLConnection.getResponseMessage());
            }
            return isSuccess;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during initialize new user {0}", e);
            showToast(context, "Error occurred during initialize new user: " + e.getMessage());
        } finally {
            if (httpURLConnection != null) httpURLConnection.disconnect();
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
        ContextCompat.getMainExecutor(context)
                .execute(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }

    private static boolean isSuccessResponse(int responseCode) {
        return responseCode >= 200 && responseCode <= 299;
    }
}
