package io.sekretess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.sekretess.BuildConfig;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.dto.AuthRequest;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private SekretessApplication sekretessApplication;


    private void authorizeUser(String username, String password) {
        sekretessApplication
                .getAuthService()
                .authorizeUser(new AuthRequest(username, password))
                .ifPresent(authResponse -> {
                    try {
                        initializeApplication();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Connection establishment failed. "
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                        finishAffinity();
                    }
                    startActivity(new Intent(this, MainActivity.class));
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == RC_AUTH) {
//            AuthorizationResponse authorizationResponse = AuthorizationResponse.fromIntent(data);
//            AuthorizationException exception = AuthorizationException.fromIntent(data);
//            if (authorizationResponse != null) {
//                TokenRequest tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest();
//                new AuthorizationService(getApplicationContext()).performTokenRequest(tokenExchangeRequest,
//                        (tokenResponse, ex) -> {
//                            if (ex != null) {
//                                Toast.makeText(getApplicationContext(), "Login failed",
//                                                Toast.LENGTH_LONG)
//                                        .show();
//                                Log.e("LoginActivity", "Error occurred during request token", ex);
//                            } else {
//                                AuthState authState = new AuthState(authorizationResponse, tokenResponse, exception);
//                                JWT jwt = new JWT(tokenResponse.accessToken);
//                                String username = jwt.getClaim(Constants.USERNAME_CLAIM).asString();
//                                Log.i("LoginActivity", "Login successful. Broadcast event.");
//                                dbHelper.storeAuthState(authState.jsonSerializeString());
//                                try {
//                                    initializeApplication();
//                                    startActivity(new Intent(this, MainActivity.class));
//                                } catch (Exception e) {
//                                    Toast.makeText(getApplicationContext(), "KeyMaterial generation failed "
//                                            + e.getMessage(), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private void initializeApplication() throws Exception {
        sekretessApplication.getSekretessWebSocketClient().startWebSocket(new URL(BuildConfig.WEB_SOCKET_URL));
        sekretessApplication.getSekretessCryptographicService().init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sekretessApplication = (SekretessApplication) getApplication();
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtUserName = findViewById(R.id.txtLoginUsername);
        TextView txtPassword = findViewById(R.id.txtLoginPassword);


        TextView textView = findViewById(R.id.txtSignupLink);
        textView.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            authorizeUser(txtUserName.getText().toString(), txtPassword.getText().toString());
        });
    }

}