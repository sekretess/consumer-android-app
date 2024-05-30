package com.sekretess.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.ApiClient;
import com.sekretess.utils.KeycloakManager;

public class LoginActivity extends AppCompatActivity {

    private DbHelper dbHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_resend_confirmation) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Resend Confirmation");
            View layoutView = getLayoutInflater().inflate(R.layout.resend_confirmation_layout, null, false);
            builder.setView(layoutView);
            builder.setPositiveButton("Resend", (dialog, which) -> {
                EditText txtResendConfirmationUsername = layoutView.findViewById(R.id.txt_resend_confirmation_username);
                if(ApiClient.resendConfirmation(txtResendConfirmationUsername.getText().toString()))
                    Toast.makeText(getApplicationContext(), "Confirmation sent", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Confirmation not sent!", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.cancel();
            });
            builder.show();
        }
        return true;
    }

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