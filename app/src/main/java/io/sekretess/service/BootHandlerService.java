package io.sekretess.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BootHandlerService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Log.i("BootHandlerService", "Starting sekrtess foreground services");

            Log.i("BootHandlerService", "Starting sekrtess SekretessRabbitMqService...");
            ContextCompat.startForegroundService(context, new Intent(context, SekretessRabbitMqService.class));
            Log.i("BootHandlerService", "Started sekrtess SekretessRabbitMqService.");


            Log.i("BootHandlerService", "Starting sekrtess RefreshTokenService...");
            ContextCompat.startForegroundService(context, new Intent(context, RefreshTokenService.class));
            Log.i("BootHandlerService", "Started sekrtess RefreshTokenService.");
        }

        Log.i("BootHandlerService", "eventReceived: " + intent.getAction());
    }
}
