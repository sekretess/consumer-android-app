package io.sekretess.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import io.sekretess.R;

public abstract class SekretessAbstractBackgroundService extends Service {

    @Override
    public final void onCreate() {
        Log.i("SekretessBackgroundService", "onCreate" + getChannelId());
        startForeground(getNotificationId(), notifyUserThatLocationServiceStarted());
        Log.i("SekretessBackgroundService", "foreground service started:" + getChannelId());
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SekretessBackgroundService", "onStartCommand" + getChannelId());
        started(intent);
        return START_STICKY;

    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        destroyed();
    }

    public abstract String getChannelId();

    public abstract int getNotificationId();

    public abstract void started(Intent intent);

    public abstract void destroyed();

    private Notification notifyUserThatLocationServiceStarted() {
        String CHANNEL_ID = getChannelId();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        notificationManager.createNotificationChannel(mChannel);
        final Notification.Builder builder;
        builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setContentTitle("Sekretess")
                .setContentText("Sekretess")
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return builder.build();
    }
}
