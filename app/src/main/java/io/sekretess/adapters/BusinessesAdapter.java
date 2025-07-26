package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.ui.BusinessInfoDialogFragment;
import io.sekretess.view.holders.BusinessesViewHolder;

import java.util.Base64;
import java.util.List;

public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesViewHolder> {

    private final List<BusinessDto> mBusinessDtos;
    private final List<String> mSubscribedBusinesses;
    private final FragmentManager fragmentManager;

    public BusinessesAdapter(List<BusinessDto> mBusinessDtos, List<String> subscribedBusinesses,
                             FragmentManager fragmentManager) {
        this.mBusinessDtos = mBusinessDtos;
        this.mSubscribedBusinesses = subscribedBusinesses;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public BusinessesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View subscriptionView = layoutInflater.inflate(R.layout.businesses_layout, parent, false);
        return new BusinessesViewHolder(subscriptionView);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position) {
        BusinessDto businessDto = mBusinessDtos.get(position);
        holder.getTxtBusinessName().setText(businessDto.getBusinessName());
        String imageBase64 = businessDto.getIcon();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.getImgBusiness().setImageBitmap(bitmap);
        }


        boolean subscribed = isSubscribed(businessDto.getBusinessName());
        if (subscribed) {
            holder.getBtnSubscribe().setImageResource(R.drawable.outline_checked_24);
        }

        holder.getImgBusiness().setOnClickListener(v -> {

            Bundle args = new Bundle();
            args.putString("businessIcon", businessDto.getIcon());
            args.putString("businessName", businessDto.getBusinessName());
            args.putBoolean("subscribed", subscribed);

            BusinessInfoDialogFragment businessInfoDialogFragment = new BusinessInfoDialogFragment();
            businessInfoDialogFragment.setArguments(args);
            businessInfoDialogFragment.show(fragmentManager, "businessInfoDialogFragment");
            //
//            View businessInfoView = LayoutInflater.from(v.getContext())
//                    .inflate(R.layout.business_info_dialog_fragment_layout, null);
//            View viewById = businessInfoView.findViewById(R.id.linearLayout);
//            BottomSheetBehavior.from(viewById).setState(BottomSheetBehavior.STATE_EXPANDED);
//
//            Context context = v.getContext();
//            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
//            bottomSheetDialog.setContentView(businessInfoView);
//            bottomSheetDialog.show();

        });
    }


    private boolean isSubscribed(String businessName) {
        if (mSubscribedBusinesses == null) return false;
        for (String subscribedBusiness : mSubscribedBusinesses) {
            if (subscribedBusiness.equalsIgnoreCase(businessName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int getItemCount() {
        return mBusinessDtos == null ? 0 : mBusinessDtos.size();
    }


}
