package io.sekretess;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.auth0.android.jwt.JWT;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.openid.appauth.AuthState;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;

import io.sekretess.repository.DbHelper;
import io.sekretess.service.RefreshTokenServiceAbstract;
import io.sekretess.service.SekretessRabbitMqService;
import io.sekretess.ui.HomeFragment;
import io.sekretess.ui.BusinessesFragment;
import io.sekretess.ui.LoginActivity;
import io.sekretess.ui.ProfileFragment;

public class MainActivity extends AppCompatActivity {

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

    private void createDbPassword(Context context) {
        SharedPreferences encryptedSharedPreferences =
                context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE);

        if (!encryptedSharedPreferences.contains("801d0837-c9c3-4a4c-bfcc-67197551d030")) {
            String p = RandomStringUtils.secureStrong().next(15);
            encryptedSharedPreferences.edit().putString("801d0837-c9c3-4a4c-bfcc-67197551d030", p)
                    .apply();
            Log.i("MainActivity", "Create password " + p);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        checkForegroundServices();
        //showBiometricLogin();
        Toast.makeText(getApplicationContext(), BuildConfig.CONSUMER_API_URL, Toast.LENGTH_LONG).show();
        prepareFileSystem();

        createDbPassword(getApplicationContext());
        Log.i("MainActivity", "OnCreate");

        Optional<AuthState> authState = restoreState();
        if (authState.isPresent()) {
            String username = new JWT(authState.get().getAccessToken()).getClaim(Constants.USERNAME_CLAIM).asString();

            registerReceiver(tokenRefreshBroadcastReceiver,
                    new IntentFilter(Constants.EVENT_TOKEN_ISSUE), RECEIVER_EXPORTED);

            setContentView(R.layout.activity_main);
            Toolbar myToolbar = findViewById(R.id.my_toolbar);
            myToolbar.setNavigationIcon(R.drawable.ic_notif_sekretess);
            setSupportActionBar(myToolbar);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

            Log.i("MainActivity", "Notify login...");
            broadcastLoginEvent(username);
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


    private void checkForegroundServices() {
        boolean isRabbitMqServiceRunning = false;
        boolean isTokenRefreshServiceRunning = false;

        ActivityManager activityManager = getSystemService(ActivityManager.class);
        while (!isRabbitMqServiceRunning && !isTokenRefreshServiceRunning) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.processName.equalsIgnoreCase("io.sekretess:remoterefreshtoken")) {
                    isTokenRefreshServiceRunning = true;
                    Log.i("MainActivity", runningAppProcessInfo.processName + " started");
                } else if (runningAppProcessInfo.processName.equalsIgnoreCase("io.sekretess:remoterabbitmq")) {
                    isRabbitMqServiceRunning = true;
                    Log.i("MainActivity", runningAppProcessInfo.processName + " started");
                }

                if (!isRabbitMqServiceRunning) {
                    Log.i("MainActivity", "Starting sekrtess SekretessRabbitMqService...");
                    ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), SekretessRabbitMqService.class));
                    Log.i("MainActivity", "Started sekrtess SekretessRabbitMqService.");
                }
                if (!isTokenRefreshServiceRunning) {
                    Log.i("MainActivity", "Starting sekrtess RefreshTokenService...");
                    ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), RefreshTokenServiceAbstract.class));
                    Log.i("MainActivity", "Started sekrtess RefreshTokenService.");
                }

                Log.i("MainActivity", "Running service " + runningAppProcessInfo.processName);
            }
        }
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
        Log.i("StartupActivity", "Restoring Authstate...");
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        if (dbHelper != null) {
            AuthState authState = dbHelper.getAuthState();
            if (authState == null) {
                Log.i("StartupActivity", "Auth state is not found");
                return Optional.empty();
            }
            Log.i("StartupActivity", "State restored.");
            return Optional.ofNullable(authState);
        } else {
            return Optional.empty();
        }
    }


    private void broadcastLoginEvent(String userName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("userName", userName);
        sendBroadcast(intent);
    }

    private void showBiometricLogin() {
        var promptInfo = new BiometricPrompt.Builder(getApplicationContext())
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();
        promptInfo.authenticate(new CancellationSignal(), getApplicationContext().getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {

        });

    }
}
