package io.sekretess.listeners;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.dto.BusinessDto;
import io.sekretess.ui.BusinessInfoDialogFragment;

public class BusinessViewOnClickListener implements View.OnClickListener {

    private final BusinessDto businessDto;

    private final int position;
    private final BusinessesAdapter businessAdapter;
    private final FragmentManager fragmentManager;

    public BusinessViewOnClickListener(BusinessDto businessDto, int position,
                                       BusinessesAdapter businessAdapter, FragmentManager fragmentManager) {
        this.businessDto = businessDto;
        this.position = position;
        this.businessAdapter = businessAdapter;
        this.fragmentManager = fragmentManager;
    }


    @Override
    public void onClick(View v) {
        Bundle args = new Bundle();
        args.putString("businessIcon", businessDto.getIcon());
        args.putString("businessName", businessDto.getBusinessName());
        args.putBoolean("subscribed", businessDto.isSubscribed());
        args.putInt("position", position);

        BusinessInfoDialogFragment businessInfoDialogFragment = new BusinessInfoDialogFragment(businessAdapter);
        businessInfoDialogFragment.setArguments(args);
        businessInfoDialogFragment.show(fragmentManager, "businessInfoDialogFragment");
    }
}
