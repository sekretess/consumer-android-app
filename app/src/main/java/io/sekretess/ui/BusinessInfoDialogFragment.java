package io.sekretess.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import io.sekretess.R;
import io.sekretess.utils.ApiClient;

public class BusinessInfoDialogFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.business_info_dialog_fragment_layout, container,
                false);
        Bundle args = getArguments();
        String icon = args.getString("businessIcon");
        String businessName = args.getString("businessName");
        boolean subscribed = args.getBoolean("subscribed");

        SwitchCompat swSubscription = view.findViewById(R.id.swSubscription);
        swSubscription.setChecked(subscribed);
        swSubscription.setOnClickListener(v -> {
            if (subscribed) {
                ApiClient.unSubscribeFromBusiness(getContext(), businessName);
            } else {
                ApiClient.subscribeToBusiness(getContext(), businessName);
            }
        });

        ((TextView) view.findViewById(R.id.businessInfoDialogBusinessNameTxt)).setText(businessName);

        if (icon != null && !icon.isEmpty()) {
            ImageView businessInfoDialogBusinessIconIv = view.findViewById(R.id.businessInfoDialogImg);
            Picasso.get().load(icon).into(businessInfoDialogBusinessIconIv);
        }


        return view;
    }
}
