package com.sekretess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.ui.ChatsActivity;
import com.sekretess.ui.LoginActivity;

public class MainActivity extends AppCompatActivity {
    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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