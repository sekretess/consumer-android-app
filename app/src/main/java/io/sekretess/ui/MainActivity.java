package io.sekretess.ui;

import android.Manifest;
import android.content.Context;
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

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.io.File;

import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.enums.SekretessEvent;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForAppUpdate();
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
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Network lost...",
                    Snackbar.LENGTH_INDEFINITE);
            SekretessDependencyProvider.getSekretessEventMutableLiveData()
                    .observe(this, sekretessEvent -> {
                        if (sekretessEvent == SekretessEvent.WEBSOCKET_CONNECTION_ESTABLISHED) {
                            snackbar.dismiss();
                        } else if (sekretessEvent == SekretessEvent.WEBSOCKET_CONNECTION_LOST) {
                            if (!snackbar.isShown()) {
                                snackbar.setGestureInsetBottomIgnored(true);
                                snackbar.setAction("Reconnect",
                                        view -> {
                                            SekretessDependencyProvider
                                                    .authenticatedWebSocket().connect();
                                        });
                                snackbar.setBehavior(new BaseTransientBottomBar.Behavior());
                                snackbar.show();
                            }
                        } else if (sekretessEvent == SekretessEvent.AUTH_FAILED) {
                            SekretessDependencyProvider.authenticatedWebSocket().disconnect();
                            startLoginActivity(getApplicationContext());
                        }
                    });
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
            startLoginActivity(this);
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
        SekretessDependencyProvider.getSekretessEventMutableLiveData().removeObservers(this);
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

        startLoginActivity(this);
    }

    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void checkForAppUpdate() {

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    || appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))) {

                appUpdateManager.startUpdateFlow(appUpdateInfo, this,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE));
            }
        });

    }

}
