package io.sekretess.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SekretessHttpInterceptor implements Interceptor {
    private final String idToken;

    public SekretessHttpInterceptor(String idToken) {
        this.idToken = idToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + idToken)
                .build();
        return chain.proceed(request);
    }
}
