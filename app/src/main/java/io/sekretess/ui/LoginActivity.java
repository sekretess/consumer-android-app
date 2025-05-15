package io.sekretess.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import io.sekretess.Constants;
import io.sekretess.MainActivity;
import io.sekretess.R;
import io.sekretess.repository.DbHelper;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginActivity extends AppCompatActivity {

    private DbHelper dbHelper;


    int RC_AUTH = 1717275371;

    private void authorizeUser() {
        String authUrl = getString(R.string.auth_url);
        AuthorizationServiceConfiguration.fetchFromUrl(Uri.parse(authUrl),
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
                }
                , uri -> {
                    URL url = new URL(uri.toString());
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    try {
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        }}, new SecureRandom());
                        httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                    } catch (Exception e) {

                    }
                    return httpsURLConnection;
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
                                broadcastSuccessfulLogin(username);
                                startActivity(new Intent(this, MainActivity.class));
                            }
                        });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        setContentView(R.layout.activity_login);
        Button btnSignup = findViewById(R.id.btnSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        btnLogin.setOnClickListener(v -> {
            authorizeUser();
        });
    }

    private void broadcastSuccessfulLogin(String queueName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("queueName", queueName);
        sendBroadcast(intent);
    }
}