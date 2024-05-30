package com.sekretess;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.service.RefreshTokenService;
import com.sekretess.service.SekretessRabbitMqService;
import com.sekretess.service.SignalProtocolService;
import com.sekretess.ui.ChatsActivity;
import com.sekretess.ui.LoginActivity;

public class MainActivity extends AppCompatActivity {
    public MainActivity() {

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isServiceRunning(SignalProtocolService.class)){
            startForegroundService(new Intent(this, SignalProtocolService.class));
        }
        if(!isServiceRunning(SekretessRabbitMqService.class)){
            startForegroundService(new Intent(this, SekretessRabbitMqService.class));
        }

        if(!isServiceRunning(RefreshTokenService.class)){
            startForegroundService(new Intent(this, RefreshTokenService.class));
        }

        Jwt jwt = new DbHelper(getApplicationContext()).getJwt();
        if (jwt != null) {
            startActivity(new Intent(this, ChatsActivity.class));
            broadcastSuccessfulLogin(jwt.getAccessToken().getPayload().getPreferredUsername());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private void broadcastSuccessfulLogin(String queueName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("queueName", queueName);
        sendBroadcast(intent);
    }
}