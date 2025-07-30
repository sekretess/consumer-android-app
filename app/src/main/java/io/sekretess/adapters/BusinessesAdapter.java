package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.ui.BusinessInfoDialogFragment;
import io.sekretess.view.holders.BusinessesViewHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesViewHolder> {

    private final List<BusinessDto> mBusinessDtos;
    private final FragmentManager fragmentManager;
    private final Context context;

    public BusinessesAdapter(Context context, List<BusinessDto> mBusinessDtos,
                             FragmentManager fragmentManager) {
        this.context = context;
        this.mBusinessDtos = mBusinessDtos;
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
    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads != null && !payloads.isEmpty()) {
            mBusinessDtos.remove(position);
            mBusinessDtos.add((BusinessDto) payloads.get(0));
        }
        super.onBindViewHolder(holder, position, payloads);
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
            holder.getImgBusiness().setScaleType(ImageView.ScaleType.CENTER_CROP);
            try {
                File baseDir = context.getFilesDir();
                File imageDir = new File(baseDir, "images");
                File imageFile = new File(imageDir, businessDto.getBusinessName()+".jpeg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        new FileOutputStream(imageFile));
            } catch (Exception e) {
                Log.e("BusinessAdapter", "Error saving image: ", e);
            }
        }

        if (businessDto.isSubscribed()) {
            holder.getBtnSubscribe().setImageResource(R.drawable.outline_checked_24);
        } else {
            holder.getBtnSubscribe().setImageBitmap(null);
        }

        holder.getImgBusiness().setOnClickListener(v -> {

            Bundle args = new Bundle();
            args.putString("businessIcon", businessDto.getIcon());
            args.putString("businessName", businessDto.getBusinessName());
            args.putBoolean("subscribed", businessDto.isSubscribed());
            args.putInt("position", position);

            BusinessInfoDialogFragment businessInfoDialogFragment = new BusinessInfoDialogFragment(this);
            businessInfoDialogFragment.setArguments(args);
            businessInfoDialogFragment.show(fragmentManager, "businessInfoDialogFragment");
        });
    }

    @Override
    public int getItemCount() {
        return mBusinessDtos == null ? 0 : mBusinessDtos.size();
    }


}
