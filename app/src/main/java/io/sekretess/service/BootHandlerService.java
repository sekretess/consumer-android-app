package io.sekretess.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.List;

import io.sekretess.repository.DbHelper;

public class BootHandlerService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BootHandlerService", intent.getAction() + " received");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            checkForegroundServices(context);
        }

        Log.i("BootHandlerService", "eventReceived: " + intent.getAction());
    }


    private void checkForegroundServices(Context context) {
        new DbHelper(context);
        boolean isRabbitMqServiceRunning = false;
        boolean isTokenRefreshServiceRunning = false;

        ActivityManager activityManager = context.getSystemService(ActivityManager.class);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            if (runningAppProcessInfo.processName.equalsIgnoreCase("io.sekretess:remoterefreshtoken")) {
                isTokenRefreshServiceRunning = true;
            } else if (runningAppProcessInfo.processName.equalsIgnoreCase("io.sekretess:remoterabbitmq")) {
                isRabbitMqServiceRunning = true;
            }

            Log.i("BootHandlerService", "Running service " + runningAppProcessInfo.processName);
        }

        if (!isRabbitMqServiceRunning) {
            Log.i("BootHandlerService", "Starting sekrtess SekretessRabbitMqService...");
            ContextCompat.startForegroundService(context, new Intent(context, SekretessRabbitMqService.class));
            Log.i("BootHandlerService", "Started sekrtess SekretessRabbitMqService.");
        }
        if (!isTokenRefreshServiceRunning) {
            Log.i("BootHandlerService", "Starting sekrtess RefreshTokenService...");
            ContextCompat.startForegroundService(context, new Intent(context, RefreshTokenService.class));
            Log.i("BootHandlerService", "Started sekrtess RefreshTokenService.");
        }
    }
}
