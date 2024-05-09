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
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.KeycloakManager;

public class LoginActivity extends AppCompatActivity {

    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbHelper(getApplicationContext());
        setContentView(R.layout.activity_login);
        Button btnSignup = findViewById(R.id.btnSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String txtUserName = ((EditText) findViewById(R.id.txtUserName)).getText().toString();
            String txtPassword = ((EditText) findViewById(R.id.txtLoginPassword)).getText().toString();
            LoginActivity.this.runOnUiThread(() -> {
            });

            Jwt jwt = KeycloakManager.getInstance().login(txtUserName, txtPassword);
            try {
                if (jwt != null) {
                    if (jwt.getRefreshExpiresIn() - 5 <= 3) {
                        Toast
                                .makeText(getApplicationContext(),
                                        "Refresh token expiration time too short",
                                        Toast.LENGTH_LONG).show();
                        return;
                    }

                    dbHelper.storeJwt(jwt.getJwtStr());
                    broadcastSuccessfulLogin(txtUserName);
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

    private void broadcastSuccessfulLogin(String queueName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("queueName", queueName);
        sendBroadcast(intent);
    }
}