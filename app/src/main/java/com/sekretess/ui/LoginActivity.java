package com.sekretess.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.ApiClient;
import com.sekretess.utils.KeycloakManager;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.Preconditions;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.browser.BrowserAllowList;
import net.openid.appauth.browser.VersionedBrowserMatcher;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
                if (ApiClient.resendConfirmation(txtResendConfirmationUsername.getText().toString()))
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

    int RC_AUTH = 1717275371;

    private void authorizeUser() {
        AuthorizationServiceConfiguration.fetchFromUrl(Constants.KEYCLOAK_OPENID_CONFIGURATION_URL,
                (serviceConfiguration, ex) -> {
                    AuthorizationRequest authorizationRequest =
                            new AuthorizationRequest.Builder(serviceConfiguration, "consumer_client",
                                    ResponseTypeValues.CODE, Constants.AUTH_REDIRECT_URL).
                                    build();

                    Intent authorizationRequestIntent = new AuthorizationService(getApplicationContext())
                            .getAuthorizationRequestIntent(authorizationRequest);
                    startActivityForResult(authorizationRequestIntent, RC_AUTH);
                }, new ConnectionBuilder() {
                    @NonNull
                    @Override
                    public HttpURLConnection openConnection(@NonNull Uri uri) throws IOException {
                        Preconditions.checkNotNull(uri, "url must not be null");
                        HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.setInstanceFollowRedirects(false);
                        return conn;
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_AUTH) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
            AuthorizationException exception = AuthorizationException.fromIntent(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            authorizeUser();
//            String txtUserName = ((EditText) findViewById(R.id.txtUserName)).getText().toString();
//            String txtPassword = ((EditText) findViewById(R.id.txtLoginPassword)).getText().toString();
//            LoginActivity.this.runOnUiThread(() -> {
//            });
//
//            Jwt jwt = KeycloakManager.getInstance().login(txtUserName, txtPassword);
//            try {
//                if (jwt != null) {
//                    if (jwt.getRefreshExpiresIn() - 5 <= 3) {
//                        Toast
//                                .makeText(getApplicationContext(),
//                                        "Refresh token expiration time too short",
//                                        Toast.LENGTH_LONG).show();
//                        return;
//                    }
//
//                    dbHelper.storeJwt(jwt.getJwtStr());
//                    broadcastSuccessfulLogin(txtUserName);
//                    startActivity(new Intent(this, ChatsActivity.class));
//                } else {
//                    try {
//                        Toast loginFailedToast = Toast.makeText(getApplicationContext(),
//                                "Login failed", Toast.LENGTH_LONG);
//                        loginFailedToast.show();
//                    } catch (Throwable t) {
//                        Log.e("LoginActivity", "Error occurred", t);
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("LoginActivity", "Error occurred during login", e);
//            }
        });
    }

    private void broadcastSuccessfulLogin(String queueName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("queueName", queueName);
        sendBroadcast(intent);
    }
}