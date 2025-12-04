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
import io.sekretess.SekretessApplication;
import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.BusinessDto;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.ImageUtils;
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
        SekretessApplication application = (SekretessApplication) requireActivity().getApplication();
        Bundle args = getArguments();
        String icon = args.getString("businessIcon");
        String businessName = args.getString("businessName");
        boolean subscribed = args.getBoolean("subscribed");

        SwitchCompat swSubscription = view.findViewById(R.id.swSubscription);
        swSubscription.setChecked(subscribed);
        swSubscription.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (SekretessDependencyProvider.apiClient().subscribeToBusiness(businessName)) {
                    businessesAdapter.subscribed(businessName);
                    businessesAdapter.rearrangeData();
                    businessesAdapter.notifyDataSetChanged();
                }

            } else {
                if (SekretessDependencyProvider.apiClient().unSubscribeFromBusiness(businessName)) {
                    businessesAdapter.unsubscribed(businessName);
                    businessesAdapter.rearrangeData();
                    businessesAdapter.notifyDataSetChanged();
                }
            }
        });

        prepareVibrationSwitch(view, businessName);
        prepareSoundAlertsSwitch(view, businessName);
        ((TextView) view.findViewById(R.id.businessInfoDialogBusinessNameTxt)).setText(businessName);

        if (icon != null && !icon.isEmpty()) {
            ImageView businessInfoDialogBusinessIconIv = view.findViewById(R.id.businessInfoDialogImg);
            businessInfoDialogBusinessIconIv.setImageBitmap(ImageUtils.bitmapFromBase64(icon));
            businessInfoDialogBusinessIconIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        return view;
    }

    private void prepareVibrationSwitch(View view, String businessName) {
        SwitchCompat swVibration = view.findViewById(R.id.swVibration);
        swVibration.setChecked(NotificationPreferencesUtils
                .getVibrationPreferences(getContext(), businessName));

        swVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationPreferencesUtils
                    .setVibrationPreferences(getContext(), businessName, isChecked);
        });
    }

    private void prepareSoundAlertsSwitch(View view, String businessName) {
        SwitchCompat swSoundAlerts = view.findViewById(R.id.swSoundAlerts);
        swSoundAlerts.setChecked(NotificationPreferencesUtils
                .getSoundAlertsPreferences(getContext(), businessName));

        swSoundAlerts.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            NotificationPreferencesUtils
                    .setSoundAlertsPreferences(getContext(), businessName, isChecked);
        }));
    }
}
