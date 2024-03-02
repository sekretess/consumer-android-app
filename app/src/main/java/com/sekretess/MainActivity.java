package com.sekretess;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.security.crypto.EncryptedSharedPreferences;

import com.sekretess.databinding.ActivityLoginBinding;
import com.sekretess.ui.SignupActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private EncryptedSharedPreferences encryptedSharedPreferences;

    public MainActivity() {
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        binding.btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
        setContentView(binding.getRoot());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}