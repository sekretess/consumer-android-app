package io.sekretess.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import io.sekretess.R;
import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.dto.BusinessDto;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.NotificationPreferencesUtils;

public class BusinessInfoDialogFragment extends BottomSheetDialogFragment {

    private final BusinessesAdapter businessesAdapter;


    public BusinessInfoDialogFragment(BusinessesAdapter businessesAdapter) {
        this.businessesAdapter = businessesAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.business_info_dialog_fragment_layout, container,
                false);
        Bundle args = getArguments();
        String icon = args.getString("businessIcon");
        String businessName = args.getString("businessName");
        boolean subscribed = args.getBoolean("subscribed");
        int position = args.getInt("position");

        SwitchCompat swSubscription = view.findViewById(R.id.swSubscription);
        swSubscription.setChecked(subscribed);
        swSubscription.setOnClickListener(v -> {
            if (subscribed) {
                if (ApiClient.unSubscribeFromBusiness(getContext(), businessName)) {
                    businessesAdapter.notifyItemChanged(position, new BusinessDto(businessName, icon, false));
                }
            } else {
                if (ApiClient.subscribeToBusiness(getContext(), businessName)) {
                    businessesAdapter.notifyItemChanged(position, new BusinessDto(businessName, icon, true));
                }
            }
        });

        prepareVibrationSwitch(view, businessName);
        prepareSoundAlertsSwitch(view, businessName);
        ((TextView) view.findViewById(R.id.businessInfoDialogBusinessNameTxt)).setText(businessName);

        if (icon != null && !icon.isEmpty()) {
            ImageView businessInfoDialogBusinessIconIv = view.findViewById(R.id.businessInfoDialogImg);
            Picasso.get().load(icon).into(businessInfoDialogBusinessIconIv);
        }

        return view;
    }

    private void prepareVibrationSwitch(View view, String businessName) {
        SwitchCompat swVibration = view.findViewById(R.id.swVibration);
        swVibration.setChecked(NotificationPreferencesUtils
                .getVibrationPreferences(getContext(), businessName));

        swVibration.setOnClickListener(v -> {
            NotificationPreferencesUtils
                    .setVibrationPreferences(getContext(), businessName, swVibration.isChecked());
        });
    }

    private void prepareSoundAlertsSwitch(View view, String businessName) {
        SwitchCompat swSoundAlerts = view.findViewById(R.id.swSoundAlerts);
        swSoundAlerts.setChecked(NotificationPreferencesUtils
                .getSoundAlertsPreferences(getContext(), businessName));

        swSoundAlerts.setOnClickListener(v -> {
            NotificationPreferencesUtils
                    .setSoundAlertsPreferences(getContext(), businessName, swSoundAlerts.isChecked());
        });
    }
}
