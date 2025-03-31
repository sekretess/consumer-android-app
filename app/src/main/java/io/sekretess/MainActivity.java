package io.sekretess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.sekretess.ui.ChatsFragment;
import io.sekretess.ui.BusinessesFragment;
import io.sekretess.ui.HomeFragment;
import io.sekretess.ui.LoginActivity;
import io.sekretess.ui.ProfileFragment;

public class MainActivity extends AppCompatActivity {


    private final BroadcastReceiver refreshTokenFailedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatActivity", "Refresh token failed event received");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       getApplicationContext().registerReceiver(refreshTokenFailedBroadcastReceiver,
                        new IntentFilter(Constants.EVENT_REFRESH_TOKEN_FAILED),RECEIVER_EXPORTED);

        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_item_business) {
                replaceFragment(new BusinessesFragment());
            } else if (item.getItemId() == R.id.menu_item_messages) {
                replaceFragment(new ChatsFragment());
            } else if (item.getItemId() == R.id.menu_item_home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.menu_item_profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
