package io.sekretess.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import io.sekretess.R;
import io.sekretess.SekretessApplication;

public class ProfileFragment extends Fragment {

    private SekretessApplication sekretessApplication;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setTitle("Profile");
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        TextView txtUserName = view.findViewById(R.id.txtUsername);

        this.sekretessApplication = (SekretessApplication) getActivity().getApplication();
        SharedPreferences globalVariables = getContext()
                .getSharedPreferences("global-variables", 0);
        String username = globalVariables.getString("username", "N/A");
        txtUserName.setText(username);
//    Delete Account button action
        AppCompatButton btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> {
            if (sekretessApplication.getApiClient().deleteUser()) {
                if (sekretessApplication.getAuthService().clearUserData()) {
                    startActivity(new Intent(ProfileFragment.this.getContext(), LoginActivity.class));
                }
            }
            sekretessApplication.getAuthService().logout();
        });


//    Logout button action
        AppCompatButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sekretessApplication.getAuthService().logout();
        });

//      Update Keys action
        AppCompatButton btnUpdateKeys = view.findViewById(R.id.btnResetKeys);
        btnUpdateKeys.setOnClickListener(v -> {
            Log.i("ProfileFragment", "Updating one time keys ");
            sekretessApplication.getSekretessCryptographicService().updateOneTimeKeys();
        });
        return view;
    }
}
