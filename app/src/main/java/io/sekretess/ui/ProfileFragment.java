package io.sekretess.ui;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.EndSessionRequest;

import io.sekretess.BuildConfig;
import io.sekretess.Constants;
import io.sekretess.MainActivity;
import io.sekretess.R;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

public class ProfileFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setTitle("Profile");
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        TextView txtUserName = view.findViewById(R.id.txtUsername);
        SharedPreferences globalVariables = getContext()
                .getSharedPreferences("global-variables", 0);
        String username = globalVariables.getString("username", "N/A");
        txtUserName.setText(username);
//    Delete Account button action
        AppCompatButton btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> {
            var dbHelper = new DbHelper(getContext());
            var authState = dbHelper.getAuthState();
            boolean deleteSuccess = false;
            if (deleteSuccess = ApiClient.deleteUser(getContext(), authState)) {
                if (deleteSuccess = dbHelper.clearUserData()) {
                    startActivity(new Intent(ProfileFragment.this.getContext(), LoginActivity.class));
                }
            }

            if (!deleteSuccess) {
                Toast.makeText(getContext(), "Account delete failed", LENGTH_LONG).show();
                logout(authState.getIdToken());
            }
        });


//    Logout button action
        AppCompatButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            var dbHelper = new DbHelper(getContext());
            var idToken = dbHelper.getAuthState().getIdToken();
            dbHelper.logout();
            logout(idToken);
        });

//      Update Keys action
        AppCompatButton btnUpdateKeys = view.findViewById(R.id.btnResetKeys);
        btnUpdateKeys.setOnClickListener(v -> {
            Log.i("ProfileFragment", "Updating one time keys ");
            MainActivity.getSekretessCryptographicService().updateOneTimeKeys();
        });
        return view;
    }


    private void logout(String idToken) {
        AuthorizationServiceConfiguration.fetchFromUrl(Uri.parse(BuildConfig.AUTH_API_URL),
                (serviceConfiguration, ex) -> {
                    EndSessionRequest endSessionRequest = new EndSessionRequest
                            .Builder(serviceConfiguration)
                            .setIdTokenHint(idToken)
                            .build();
                    Intent endSessionRequestIntent = new AuthorizationService(getContext())
                            .getEndSessionRequestIntent(endSessionRequest);
                    startActivity(endSessionRequestIntent);
                });
    }
}
