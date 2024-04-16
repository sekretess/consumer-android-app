package com.sekretess.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.utils.KeycloakManager;
import com.sekretess.service.SignalProtocolService;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sekretesSharedPreferences = getApplication()
                .getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sekretesSharedPreferences.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btnSignup = findViewById(R.id.btnSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        btnLogin.setOnClickListener(v -> {
            EditText txtEmailAddress = findViewById(R.id.txtEmailAddress);
            EditText txtPassword = findViewById(R.id.txtLoginPassword);
            LoginActivity.this.runOnUiThread(() -> {
            });

            Jwt jwt = KeycloakManager.getInstance().login(txtEmailAddress.getText().toString(),
                    txtPassword.getText().toString());
            try {
                if (jwt.getRefreshExpiresIn() - 5 <= 3) {
                    Toast
                            .makeText(getApplicationContext(),
                                    "Refresh token expiration time too short",
                                    Toast.LENGTH_LONG).show();
                    return;
                }
                if (jwt != null) {

                    sharedPreferencesEditor
                            .putString(Constants.PREFERENCES_JWT_PROPERTY_NAME, jwt.getJwtStr());
                    sharedPreferencesEditor.apply();
                    sharedPreferencesEditor.commit();

//                    SignalProtocolService.getInstance().initializeKeysFromJwt(jwt);

                    startActivity(new Intent(this, ChatsActivity.class));
                } else {
                    try {
                        Toast loginFailedToast = Toast.makeText(getApplicationContext(),
                                "Login failed", Toast.LENGTH_LONG);
                        loginFailedToast.show();
                    } catch (Throwable t) {
                        Log.e("LoginActivity", "Error occurred", t);
                    }
                }
            } catch (Exception e) {
                Log.e("LoginActivity", "Error occurred during login", e);
            }
        });


    }
}