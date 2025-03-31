package io.sekretess.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.sekretess.R;

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
        return view;
    }
}
