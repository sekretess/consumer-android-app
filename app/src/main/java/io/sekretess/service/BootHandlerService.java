package io.sekretess.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootHandlerService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Log.i("BootHandlerService", "Starting sekrtess foreground services");
            context.startForegroundService(new Intent(context, SignalProtocolService.class));
            context.startForegroundService(new Intent(context, RefreshTokenService.class));
            context.startForegroundService(new Intent(context, SekretessRabbitMqService.class));
        }

        Log.i("BootHandlerService", "eventReceived: " + intent.getAction());
    }
}
