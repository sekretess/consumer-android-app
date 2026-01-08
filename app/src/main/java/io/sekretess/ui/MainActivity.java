package io.sekretess.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

import io.sekretess.BuildConfig;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.db.SekretessDatabase;
import io.sekretess.dependency.SekretessDependencyProvider;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SekretessApplication sekretessApplication = (SekretessApplication) getApplication();
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        //showBiometricLogin();
        prepareFileSystem();
        Log.i("MainActivity", "OnCreate");
        if (SekretessDependencyProvider.authService().isAuthorized()) {
            try {
                Log.i("MainActivity", "Initializing app");
                if (!SekretessDependencyProvider.cryptographicService().init()) {
                    onCryptoKeyInitFailed();
                    return;
                }
                SekretessDependencyProvider.authenticatedWebSocket().connect();
            } catch (Exception e) {
                Log.e(TAG, "Error occurred during initialization app", e);
                onCryptoKeyInitFailed();
                return;
            }
            sekretessApplication.registerNetworkStatusMonitor();
            setContentView(R.layout.activity_main);
            Toolbar myToolbar = findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

            Log.i("MainActivity", "Notify login...");
            replaceFragment(new HomeFragment());
//            SekretessDependencyProvider.messageService().insertTestData();
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.menu_item_business) {
                    replaceFragment(new BusinessesFragment());
                } else if (item.getItemId() == R.id.menu_item_home) {
                    replaceFragment(new HomeFragment());
                } else if (item.getItemId() == R.id.menu_item_profile) {
                    replaceFragment(new ProfileFragment());
                }
                return true;
            });
        } else {
            Log.i("MainActivity", "Starting login activity");
            startLoginActivity();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private void prepareFileSystem() {
        File baseDir = getApplicationContext().getFilesDir();
        File imageDir = new File(baseDir, "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void onCryptoKeyInitFailed() {
        finish();
        try {
            SekretessDependencyProvider.authService().logout();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during logout", e);
        }

        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
