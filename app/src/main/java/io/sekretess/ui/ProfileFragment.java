package io.sekretess.ui;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import io.sekretess.R;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

public class ProfileFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        TextView txtUserName = view.findViewById(R.id.txtUsername);
        SharedPreferences globalVariables = getContext().getApplicationContext()
                .getSharedPreferences("global-variables", 0);
        String username = globalVariables.getString("username", "N/A");
        txtUserName.setText(username);

        AppCompatImageView btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            var dbHelper = DbHelper.getInstance(getContext());
            var idToken = dbHelper.getAuthState().getIdToken();
            boolean deleteSuccess = false;
            if (deleteSuccess = ApiClient.deleteUser(idToken)) {
                if (deleteSuccess= dbHelper.clearUserData()) {
                    startActivity(new Intent(ProfileFragment.this.getContext(), LoginActivity.class));
                }
            }

            if(!deleteSuccess){
                Toast.makeText(getContext(), "Account delete failed", LENGTH_LONG).show();
            }
        });
        return view;
    }
}
