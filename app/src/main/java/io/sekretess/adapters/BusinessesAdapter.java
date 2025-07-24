package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.ui.ButtonClickListener;
import io.sekretess.view.holders.BusinessesViewHolder;

import java.util.Base64;
import java.util.List;

public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesViewHolder> {

    private final List<BusinessDto> mBusinessDtos;
    private final List<String> mSubscribedBusinesses;

    public BusinessesAdapter(List<BusinessDto> mBusinessDtos, List<String> subscribedBusinesses) {
        this.mBusinessDtos = mBusinessDtos;
        this.mSubscribedBusinesses = subscribedBusinesses;
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
        if (isSubscribed(businessDto.getBusinessName())) {
           holder.getBtnSubscribe().setImageResource(R.drawable.outline_remove_24);
        }else{
            holder.getBtnSubscribe().setImageResource(R.drawable.outline_add_24);
        }
//        holder.getBtnSubscribe()
//                .setOnClickListener(new ButtonClickListener(businessDto.getBusinessName()));

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
