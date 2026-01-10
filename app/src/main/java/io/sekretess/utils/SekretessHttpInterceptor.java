package io.sekretess.utils;

import java.io.IOException;

import io.sekretess.dependency.SekretessDependencyProvider;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SekretessHttpInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            String accessToken = SekretessDependencyProvider.authService().getAccessToken().toString();
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            return chain.proceed(request);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
