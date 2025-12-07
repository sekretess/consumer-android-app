package io.sekretess.listeners;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.lifecycle.LiveData;

public class NetworkStatusListener extends LiveData<Boolean> {


    private final ConnectivityManager connectivityManager;
    private final NetworkRequest networkRequest;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkStatusListener(Context context) {
        connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                postValue(true);
            }

            @Override
            public void onLost(Network network) {
                postValue(false);
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        NetworkCapabilities caps =
                connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        boolean connected = caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        postValue(connected);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

}
