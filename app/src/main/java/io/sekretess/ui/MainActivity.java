package io.sekretess.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.auth0.android.jwt.JWT;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import net.openid.appauth.AuthState;

import java.io.File;
import java.util.Optional;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.repository.DbHelper;

public class MainActivity extends AppCompatActivity {
    private SekretessApplication application;
    private final BroadcastReceiver tokenRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MainActivity", "Refresh token failed");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    };

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.application = (SekretessApplication) getApplication();


        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast

                    Log.d("MainActivity", "FCM Token " + token);

                });
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        //showBiometricLogin();
        prepareFileSystem();
        Log.i("MainActivity", "OnCreate");
        Optional<AuthState> authState = restoreState();
        if (authState.isPresent()) {
            String username = new JWT(authState.get().getAccessToken()).getClaim(Constants.USERNAME_CLAIM).asString();
            setContentView(R.layout.activity_main);
            Toolbar myToolbar = findViewById(R.id.my_toolbar);
//            myToolbar.setNavigationIcon(R.drawable.ic_notif_sekretess);
            setSupportActionBar(myToolbar);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

            Log.i("MainActivity", "Notify login...");
            replaceFragment(new HomeFragment());
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

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private Optional<AuthState> restoreState() {
        Log.i("MainActivity", "Restoring Authstate...");
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        if (dbHelper != null) {
            AuthState authState = dbHelper.getAuthState();
            if (authState == null) {
                Log.i("MainActivity", "Auth state is not found");
                return Optional.empty();
            }
            Log.i("MainActivity", "State restored.");
            return Optional.ofNullable(authState);
        } else {
            return Optional.empty();
        }
    }
}
