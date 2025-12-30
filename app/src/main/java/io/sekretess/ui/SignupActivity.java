package io.sekretess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.KeyBundle;
import io.sekretess.utils.ApiClient;

public class SignupActivity extends AppCompatActivity {
    private SekretessApplication sekretessApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sekretessApplication = (SekretessApplication) getApplication();

        setContentView(R.layout.activity_signup);
        Button btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(v -> signup());

        TextView txtLoginLink = findViewById(R.id.txtLoginLink);
        txtLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void signup() {

        String email = ((TextInputEditText) findViewById(R.id.txtSignupEmail)).getText().toString();

        TextInputEditText userNameEdit = findViewById(R.id.txtSignupUsername);
        TextInputEditText passwordEdit = findViewById(R.id.txtSignupPassword);
        TextInputEditText confirmPasswordEdit = findViewById(R.id.txtPasswordVerify);


        String username = userNameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (!(validateUserName(userNameEdit)) || !validatePassword(passwordEdit)) {
            return;
        }

        if (!confirmPasswordEdit.getText().toString().equals(passwordEdit.getText().toString())) {
            confirmPasswordEdit.setError("Not matched with password");
            return;
        }

        KeyBundle keyBundle = SekretessDependencyProvider.cryptographicService()
                .initializeKeyBundle();
        SekretessDependencyProvider.apiClient().createUser(username, email, password, keyBundle);
    }

    private boolean validateUserName(EditText usernameEdit) {
        String username = usernameEdit.getText().toString();
        if (username.length() <= 4) {
            usernameEdit.setError("Username length should be more than 4");
            return false;
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("SignupActivity", "New intent received");
    }

    private boolean validatePassword(EditText passwordEdit) {
        String password = passwordEdit.getText().toString();
        if (password.length() < 8) {
            passwordEdit.setError("Password should be at least 8 characters long");
            return false;
        }



        if (!password.matches("(.*)[A-Z]+(.*)")) {
            passwordEdit.setError("Password must contain at least one capital letter");
            return false;
        }


        if (!password.matches("(.*)[a-z]+(.*)")) {
            passwordEdit.setError("Password must contain at least one lowercase letter");
            return false;
        }


        if (!password.matches("(.*)[!@#$%^&*()]+(.*)")) {
            passwordEdit.setError("Password must contain at least one special character !@#$%^&*()");
            return false;
        }

        if (!password.matches("(.*)[0-9]+(.*)")) {
            passwordEdit.setError("Password must contain at least one number");
            return false;
        }


        return true;
    }


}