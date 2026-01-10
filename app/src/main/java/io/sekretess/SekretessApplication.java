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
    private SekretessNetworkMonitor sekretessNetworkMonitor;


    @Override
    public void onCreate() {
        super.onCreate();
        new SekretessDependencyProvider(getApplicationContext());
    }

    public void registerNetworkStatusMonitor() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NET_CAPABILITY_INTERNET)
                .build();

        if (sekretessNetworkMonitor != null) {
            unregisterNetworkStatusMonitor();
        }

        this.sekretessNetworkMonitor = new SekretessNetworkMonitor();
        connectivityManager.registerNetworkCallback(request, sekretessNetworkMonitor);
    }

    private void unregisterNetworkStatusMonitor() {
        if (sekretessNetworkMonitor != null) {
            connectivityManager.unregisterNetworkCallback(sekretessNetworkMonitor);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterNetworkStatusMonitor();
        SekretessDependencyProvider.authenticatedWebSocket().disconnect();
    }
}
