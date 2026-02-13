package io.sekretess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import io.sekretess.BuildConfig;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.AuthRequest;
import io.sekretess.exception.UnAuhthorizedException;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private void authorizeUser(String username, String password) throws UnAuhthorizedException {
        SekretessDependencyProvider.authService()
                .authorize(new AuthRequest(username, password));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextInputEditText txtUserName = findViewById(R.id.txtLoginUsername);
        TextInputEditText txtPassword = findViewById(R.id.txtLoginPassword);


        TextView textView = findViewById(R.id.txtSignupLink);
        textView.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            Log.i("LoginActivity", "Login Button clicked");
            try {
                authorizeUser(txtUserName.getText().toString(), txtPassword.getText().toString());
                startActivity(new Intent(this, MainActivity.class));
            } catch (Exception e) {
                Log.e("LoginActivity", "Login failed", e);
                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
            }
        });
    }

}