package io.sekretess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import io.sekretess.Constants;
import io.sekretess.R;

public class SignupActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        Button btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(v -> broadcastSignup());
    }

    private void broadcastSignup (){

        String email = ((EditText) findViewById(R.id.txtSignupEmail)).getText().toString();

        EditText userNameEdit = findViewById(R.id.txtSignupUsername);
        EditText passwordEdit = findViewById(R.id.txtSignupPassword);
        EditText confirmPasswordEdit = findViewById(R.id.txtPasswordVerify);


        String username = userNameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (!(validateUserName(userNameEdit)) || !validatePassword(passwordEdit)) {
            return;
        }

        if (!confirmPasswordEdit.getText().toString().equals(passwordEdit.getText().toString())){
            confirmPasswordEdit.setError("Not matched with password");
            return;
        }

            Intent intent = new Intent(Constants.EVENT_SIGNUP);
        intent.putExtra("email", email);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        sendBroadcast(intent);
    }

    private boolean validateUserName(EditText usernameEdit) {
        String username = usernameEdit.getText().toString();
        if (username.length() <= 4) {
            usernameEdit.setError("Username length should be more than 4");
            return false;
        }
        return true;
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