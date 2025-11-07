package io.sekretess.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import io.sekretess.BuildConfig;
import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.repository.DbHelper;
import io.sekretess.service.SekretessWebSocketClient;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private SekretessApplication sekretessApplication;

    int RC_AUTH = 1717275371;

    private void authorizeUser() {
        AuthorizationServiceConfiguration.fetchFromUrl(Uri.parse(BuildConfig.AUTH_API_URL),
                (serviceConfiguration, ex) -> {
                    AuthorizationRequest authorizationRequest =
                            new AuthorizationRequest.Builder(serviceConfiguration, "consumer_client",
                                    ResponseTypeValues.CODE,
                                    Constants.AUTH_REDIRECT_URL).
                                    setScopes("email", "roles", "profile", "web-origins", "acr", "openid").
                                    build();
                    Intent authorizationRequestIntent = new AuthorizationService(getApplicationContext())
                            .getAuthorizationRequestIntent(authorizationRequest);
                    startActivityForResult(authorizationRequestIntent, RC_AUTH);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_AUTH) {
            AuthorizationResponse authorizationResponse = AuthorizationResponse.fromIntent(data);
            AuthorizationException exception = AuthorizationException.fromIntent(data);
            if (authorizationResponse != null) {
                TokenRequest tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest();
                new AuthorizationService(getApplicationContext()).performTokenRequest(tokenExchangeRequest,
                        (tokenResponse, ex) -> {
                            if (ex != null) {
                                Toast.makeText(getApplicationContext(), "Login failed",
                                                Toast.LENGTH_LONG)
                                        .show();
                                Log.e("LoginActivity", "Error occurred during request token", ex);
                            } else {
                                AuthState authState = new AuthState(authorizationResponse, tokenResponse, exception);
                                JWT jwt = new JWT(tokenResponse.accessToken);
                                String username = jwt.getClaim(Constants.USERNAME_CLAIM).asString();
                                Log.i("LoginActivity", "Login successful. Broadcast event.");
                                dbHelper.storeAuthState(authState.jsonSerializeString());
                                try {
                                    initializeApplication();
                                    startActivity(new Intent(this, MainActivity.class));
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "KeyMaterial generation failed "
                                            + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeApplication() throws Exception {
        sekretessApplication.getSekretessWebSocketClient().startWebSocket();
        sekretessApplication.getSekretessCryptographicService().init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sekretessApplication = (SekretessApplication) getApplication();

        dbHelper = new DbHelper(getApplicationContext());
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);

        TextView textView = findViewById(R.id.txtSignupLink);
        textView.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            authorizeUser();
        });
    }

}