package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.listeners.BusinessViewOnClickListener;
import io.sekretess.ui.BusinessInfoDialogFragment;
import io.sekretess.utils.ImageUtils;
import io.sekretess.view.holders.BusinessesViewHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesViewHolder> implements Filterable {

    private final List<BusinessDto> mBusinessDtos;
    private final List<BusinessDto> filteredList = new ArrayList<>();
    private final FragmentManager fragmentManager;
    private final Context context;
    private boolean filtered;

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

//    @Override
//    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position, @NonNull List<Object> payloads) {
//        if (payloads != null && !payloads.isEmpty()) {
//            mBusinessDtos.remove(position);
//            mBusinessDtos.add(position, (BusinessDto) payloads.get(0));
//        }
//        super.onBindViewHolder(holder, position, payloads);
//    }

    @Override
    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position) {
        List<BusinessDto> source = filtered ? filteredList : mBusinessDtos;
        if(position >= source.size())
            return;
        BusinessDto businessDto = source.get(position);
        holder.getTxtBusinessName().setText(businessDto.getBusinessName());
        String imageBase64 = businessDto.getIcon();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap bitmap = ImageUtils.bitmapFromBase64(imageBase64);
            holder.getImgBusiness().setImageBitmap(bitmap);
            holder.getImgBusiness().setScaleType(ImageView.ScaleType.CENTER_CROP);
            try {
                File baseDir = context.getFilesDir();
                File imageDir = new File(baseDir, "images");
                File imageFile = new File(imageDir, businessDto.getBusinessName() + ".jpeg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        new FileOutputStream(imageFile));
            } catch (Exception e) {
                Log.e("BusinessAdapter", "Error saving image: ", e);
            }
        }else{
            holder.getImgBusiness().setImageResource(R.drawable.round_add_moderator_24);
        }

        if (businessDto.isSubscribed()) {
            holder.getTxtSubscriptionStatus().setText("Subscribed");
        } else {
            holder.getTxtSubscriptionStatus().setText("Not subscribed");
        }
        BusinessViewOnClickListener businessViewOnClickListener =
                new BusinessViewOnClickListener(businessDto, position, this, fragmentManager);
        holder.itemView.findViewById(R.id.businesses_layout).setOnClickListener(businessViewOnClickListener);
        holder.getImgBusiness().setOnClickListener(businessViewOnClickListener);
    }

    @Override
    public int getItemCount() {
        return filtered ? filteredList.size() : mBusinessDtos == null ? 0 : mBusinessDtos.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<BusinessDto> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtered = false;
                    results.values = mBusinessDtos;
                    return results;
                } else {
                    filtered = true;
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (BusinessDto businessDto : mBusinessDtos) {
                        if (businessDto.getBusinessName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(businessDto);
                        }
                    }
                    results.values = filteredList;
                    return results;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List<BusinessDto>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private Optional<BusinessDto> findBusinessByName(String name){
        return mBusinessDtos
                .stream()
                .filter(businessDto -> businessDto
                        .getBusinessName()
                        .equalsIgnoreCase(name))
                .findFirst();
    }

    public void subscribed(String businessName) {
        findBusinessByName(businessName)
                .ifPresent(businessDto -> businessDto.setSubscribed(true));
    }

    public void unsubscribed(String businessName) {
        findBusinessByName(businessName)
                .ifPresent(businessDto -> businessDto.setSubscribed(false));
    }
}
