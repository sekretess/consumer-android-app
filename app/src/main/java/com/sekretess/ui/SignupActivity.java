package com.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sekretess.Constants;
import com.sekretess.R;

public class SignupActivity extends AppCompatActivity {

    private final BroadcastReceiver signupFailedEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SignupActivity", "signup-failed");
            Toast.makeText(getApplicationContext(), "User creation failed",
                            Toast.LENGTH_LONG)
                    .show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(signupFailedEventBroadcastReceiver,
                new IntentFilter(Constants.EVENT_SIGNUP_FAILED), RECEIVER_EXPORTED);
    }

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
        Intent intent = new Intent(Constants.EVENT_INITIALIZE_KEY);
        intent.putExtra("email", email);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        sendBroadcast(intent);
    }

}