package io.sekretess.service;

import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.sekretess.dependency.SekretessDependencyProvider;

public class SekretessNetworkMonitor extends ConnectivityManager.NetworkCallback {


    public SekretessNetworkMonitor() {

    }

    @Override
    public void onAvailable(Network network) {
        Log.i("SekretessNetworkMonitor", "Network available");
        SekretessDependencyProvider.authenticatedWebSocket().connect();

    }

    @Override
    public void onLost(Network network) {
        Log.i("SekretessNetworkMonitor", "Network lost");
        SekretessDependencyProvider.authenticatedWebSocket().disconnect();

    }
}
