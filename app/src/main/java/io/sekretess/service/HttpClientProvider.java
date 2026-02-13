package io.sekretess.service;

import io.sekretess.utils.BearerAuthenticator;
import io.sekretess.utils.SekretessHttpInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClientProvider {

    private static final OkHttpClient authorizedHttpClient;
    private static final OkHttpClient anonymousHttpClient;


    static {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Set the logging level (BODY logs headers and body)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        authorizedHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new SekretessHttpInterceptor())
                .authenticator(new BearerAuthenticator())
                .build();


        anonymousHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    public static OkHttpClient anonymousHttpClient() {
        return anonymousHttpClient;
    }

    public static OkHttpClient authorizedHttpClient() {
        return authorizedHttpClient;
    }


}
