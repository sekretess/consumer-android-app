package com.sekretess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.dto.jwt.Jwt;
import com.sekretess.service.SekretessRabbitMqService;
import com.sekretess.ui.ChatsActivity;
import com.sekretess.ui.LoginActivity;
import com.sekretess.service.SignalProtocolService;

import org.signal.libsignal.protocol.InvalidMessageException;

public class MainActivity extends AppCompatActivity {


    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sekretesSharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String jwtStr = sekretesSharedPreferences
                .getString(Constants.PREFERENCES_JWT_PROPERTY_NAME, "");

        if (!jwtStr.isEmpty()) {
            Jwt jwt = Jwt.fromString(jwtStr);
            String queueName = jwt.getAccessToken().getPayload().getPreferredUsername();
            Intent backgroundRabbitMqConsumerService =
                    new Intent(this, SekretessRabbitMqService.class);
            backgroundRabbitMqConsumerService.putExtra("queueName", queueName);
            backgroundRabbitMqConsumerService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(backgroundRabbitMqConsumerService);
            startService(new Intent(this, SignalProtocolService.class));
            startActivity(new Intent(this, ChatsActivity.class));
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

}