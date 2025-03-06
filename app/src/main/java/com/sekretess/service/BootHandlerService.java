package com.sekretess.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootHandlerService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startForegroundService(new Intent(context, SignalProtocolService.class));
            context.startForegroundService(new Intent(context, RefreshTokenService.class));
            context.startForegroundService(new Intent(context, SekretessRabbitMqService.class));
        }
    }
}
