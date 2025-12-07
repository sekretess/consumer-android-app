package io.sekretess;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;

import androidx.lifecycle.MutableLiveData;

import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.service.SekretessNetworkMonitor;

public class SekretessApplication extends Application {

    private ConnectivityManager connectivityManager;

    @Override
    public void onCreate() {
        super.onCreate();
        new SekretessDependencyProvider(getApplicationContext());

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, new SekretessNetworkMonitor());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SekretessDependencyProvider.authenticatedWebSocket().disconnect();
    }
}
