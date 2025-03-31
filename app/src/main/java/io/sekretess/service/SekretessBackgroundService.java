package io.sekretess.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.PowerManager;

import io.sekretess.R;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class SekretessBackgroundService extends Service {

    @Override
    public final void onCreate() {
        super.onCreate();
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(getNotificationId(), notifyUserThatLocationServiceStarted());
        started(intent);
        return START_STICKY;

    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
//        wakeLock.release();
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
        final Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setContentTitle("Sekretess")
                .setContentText("Sekretess")
                .setWhen(System.currentTimeMillis());
        return builder.build();
    }
}
