package com.sekretess.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.KeyMaterial;
import com.sekretess.service.SekretessRabbitMqService;
import com.sekretess.utils.KeycloakManager;
import com.sekretess.service.SignalProtocolService;

import java.util.Base64;
import java.util.Set;

public class SignupActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Button btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(v -> initializeNewUser());
    }

    private void initializeNewUser() {
        try {
            String email = ((EditText) findViewById(R.id.txtSignupEmail)).getText().toString();
            String username = ((EditText) findViewById(R.id.txtSignupUsername)).getText().toString();
            String password = ((EditText) findViewById(R.id.txtSignupPassword)).getText().toString();

            broadcastInitializeKeys(email, username, password);

        } catch (Exception e) {
            Log.e("SignupActivity", "Error occurred during initialize new user", e);
        }
    }

    private void broadcastInitializeKeys(String email, String username, String password) {
        Intent intent = new Intent("initialize-key-event");
        intent.putExtra("email", email);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        LocalBroadcastManager.getInstance(SignupActivity.this).sendBroadcast(intent);
    }

}