package io.sekretess.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.prefs.Preferences;

public class NotificationPreferencesUtils {

    public static void setVibrationPreferences(Context context, String businessName, boolean value) {
        writePreference(context, "vibration_" + businessName, value);
    }

    public static boolean getVibrationPreferences(Context context, String businessName) {
        return readPreference(context, "vibration_" + businessName);
    }

    public static void setSoundAlertsPreferences(Context context, String businessName, boolean value) {
        writePreference(context, "sound_alerts_" + businessName, value);
    }

    public static boolean getSoundAlertsPreferences(Context context, String businessName) {
        Log.i("NotificationPreferencesUtils", "getSoundAlertsPreferences" + businessName);
        return readPreference(context, "sound_alerts_" + businessName);
    }

    private static SharedPreferences getSharedPreference(Context context) {
        return context
                .getSharedPreferences("notification_preferences", Context.MODE_MULTI_PROCESS);
    }


    private static void writePreference(Context context, String preferenceName, boolean value) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        SharedPreferences.Editor notificationPreferencesEditor = sharedPreferences.edit();
        notificationPreferencesEditor.putBoolean(preferenceName, value);
        notificationPreferencesEditor.apply();

    }

    private static boolean readPreference(Context context, String preferenceName) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        return sharedPreferences.getBoolean(preferenceName, true);
    }


}
