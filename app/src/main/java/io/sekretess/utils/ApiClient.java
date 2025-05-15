package io.sekretess.utils;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sekretess.Constants;
import io.sekretess.dto.BusinessDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean subscribeToBusiness(String business, String jwt) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> subscribeToBusinessInternal(business, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait subscribe to business api result", e);
            return false;
        }
    }

    private static boolean subscribeToBusinessInternal(String business, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses/"
                    + business);
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            Log.i("ApiClient", "Subscribe to business resultcode "+ urlConnection.getResponseCode());
            return urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode() <= 299;
        } catch (Exception e) {
            Log.e("ApiClient"
                    , "Error occurred during subscribe to " + business, e);
            return false;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    public static boolean unSubscribeFromBusiness(String business, String jwt) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> unSubscribeFromBusinessInternal(business, jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during wait unsubscribe from business thread", e);
            return false;
        }
    }

    private static boolean unSubscribeFromBusinessInternal(String business, String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses/"
                    + business);
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            return urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode() <= 299;
        } catch (Exception e) {
            Log.e("ApiClient"
                    , "Error occurred during unsubscribe from " + business, e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static void refreshChannelSubscription(String jwt) {
        try {
            Executors
                    .newSingleThreadExecutor()
                    .submit(() -> refreshChannelSubscriptionInternal(jwt));
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during refresh channel subscription.", e);
        }
    }

    private static void refreshChannelSubscriptionInternal(String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL consumerApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses/refresh-subscription");
            urlConnection = (HttpURLConnection) consumerApiUrl.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            urlConnection.getInputStream().read();
            Log.i("ApiClient", "Refresh channel subscription API called");
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during refresh channel subscription", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    public static List<BusinessDto> getBusinesses() {
        try {
            Future<List<BusinessDto>> future = Executors
                    .newSingleThreadExecutor()
                    .submit(ApiClient::getBusinessesInternal);
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses.", e);
        }
        return Collections.EMPTY_LIST;
    }

    private static List<BusinessDto> getBusinessesInternal() {
        HttpURLConnection urlConnection = null;
        try {
            URL businessApiUrl = new URL(Constants.BUSINESS_API_URL);
            urlConnection = (HttpURLConnection) businessApiUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            if (urlConnection.getResponseCode() > 299) {
                Log.e("ApiClient", "HTTP error " + urlConnection.getResponseCode() + " URL: " +Constants.BUSINESS_API_URL);
                return Collections.EMPTY_LIST;
            }
            StringBuilder response = new StringBuilder();
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            List<BusinessDto> businessDtos = objectMapper.readValue(response.toString(), new TypeReference<List<BusinessDto>>() {
            });
            return businessDtos;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses {}", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return Collections.EMPTY_LIST;
    }

    public static List<String> getSubscribedBusinesses(String jwt) {
        try {
            Future<List<String>> future = Executors.newSingleThreadExecutor()
                    .submit(() -> getSubscribedBusinessesInternal(jwt));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get subscribed businesses.", e);
        }
        return Collections.EMPTY_LIST;
    }

    private static List<String> getSubscribedBusinessesInternal(String jwt) {
        HttpURLConnection urlConnection = null;
        try {
            URL businessApiUrl = new URL(Constants.CONSUMER_API_URL + "/ads/businesses");
            urlConnection = (HttpURLConnection) businessApiUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            if (urlConnection.getResponseCode() > 299) {
                Log.e("ApiClient", "HTTP error " + urlConnection.getResponseCode() + " URL: " +urlConnection.getURL().toString());
                return Collections.EMPTY_LIST;
            }
            StringBuilder response = new StringBuilder();
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            List result = objectMapper.readValue(response.toString(), List.class);
            if (result == null)
                return Collections.EMPTY_LIST;
            return result;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during get businesses", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return Collections.EMPTY_LIST;
    }

    public static boolean resendConfirmation(String username) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor()
                    .submit(() -> resendConfirmationInternal(username));
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during resend confirmation email", e);
        }

        return false;
    }

    private static boolean resendConfirmationInternal(String username) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL resendConfirmationUrl = new URL(Constants.CONSUMER_API_URL + "/" + username + "/auth/resend-email");
            httpURLConnection = (HttpURLConnection) resendConfirmationUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            httpURLConnection.getInputStream().read();
            return httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() <= 299;
        } catch (Exception e) {
            Log.e("ApiClient", "Error occurred during request confirmation email", e);
            return false;
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }
}
