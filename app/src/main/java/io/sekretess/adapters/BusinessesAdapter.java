package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.enums.ItemType;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    public void rearrangeData() {
        final AtomicBoolean subscribed = new AtomicBoolean(false);
        List<BusinessDto> source = findSource();
        List<BusinessDto> list = findSource().stream()
                .sorted((o1, o2) -> o1.isSubscribed() ? -1 : 1)
                .peek(businessDto -> {
                    if (subscribed.get() != businessDto.isSubscribed()) {
                        subscribed.set(businessDto.isSubscribed());
                        businessDto.setItemType(ItemType.HEADER);
                    }else{
                        businessDto.setItemType(ItemType.ITEM);
                    }
                })
                .toList();
        source.clear();
        source.addAll(list);
    }

//    @Override
//    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position, @NonNull List<Object> payloads) {
//        if (payloads != null && !payloads.isEmpty()) {
//            mBusinessDtos.remove(position);
//            mBusinessDtos.add(position, (BusinessDto) payloads.get(0));
//        }
//        super.onBindViewHolder(holder, position, payloads);
//    }

    private List<BusinessDto> findSource() {
        return filtered ? filteredList : mBusinessDtos;
    }

    @Override
    public int getItemViewType(int position) {
        List<BusinessDto> source = findSource();
        BusinessDto businessDto = source.get(position);
        return businessDto.getItemType() == ItemType.HEADER ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position) {
        List<BusinessDto> source = findSource();
        if (position >= source.size())
            return;
        BusinessDto businessDto = source.get(position);
        holder.getTxtBusinessName().setText(businessDto.getDisplayName());
        String imageBase64 = businessDto.getIcon();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap bitmap = ImageUtils.bitmapFromBase64(imageBase64);
            holder.getImgBusiness().setImageBitmap(bitmap);
            holder.getImgBusiness().setScaleType(ImageView.ScaleType.CENTER_CROP);
            try {
                File baseDir = context.getFilesDir();
                File imageDir = new File(baseDir, "images");
                File imageFile = new File(imageDir, businessDto.getName() + ".jpeg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        new FileOutputStream(imageFile));
            } catch (Exception e) {
                Log.e("BusinessAdapter", "Error saving image: ", e);
            }
        } else {
            holder.getImgBusiness().setImageResource(R.drawable.round_add_moderator_24);
        }

        TextView txtSubscriptionStatus = holder.itemView.findViewById(R.id.txtSubscribedHeader);
        if (businessDto.getItemType() == ItemType.HEADER) {
            txtSubscriptionStatus.setVisibility(View.VISIBLE);
            if (businessDto.isSubscribed()) {
                txtSubscriptionStatus.setText("Subscribed");
            } else {
                txtSubscriptionStatus.setText("Other Businesses");
            }
        } else {
            txtSubscriptionStatus.setVisibility(View.GONE);
        }

        if (businessDto.isSubscribed()) {
            holder.getTxtSubscriptionStatus().setText("Subscribed");
            holder.itemView
                    .findViewById(R.id.constraintLayout3)
                    .setBackground(context.getDrawable(R.drawable.oval_gradient_layout_background));
        } else {
            holder.getTxtSubscriptionStatus().setText("Not subscribed");
            holder.itemView
                    .findViewById(R.id.constraintLayout3)
                    .setBackground(context.getDrawable(R.drawable.rounded_shape));
        }
        BusinessViewOnClickListener businessViewOnClickListener =
                new BusinessViewOnClickListener(businessDto, position, this, fragmentManager);
        holder.itemView.findViewById(R.id.businesses_layout).setOnClickListener(businessViewOnClickListener);
        holder.getImgBusiness().setOnClickListener(businessViewOnClickListener);
    }

    @Override
    public int getItemCount() {
        List<BusinessDto> source = findSource();
        return source == null ? 0 : source.size();
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
                        if (businessDto.getName().toLowerCase().contains(filterPattern)) {
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

    private Optional<BusinessDto> findBusinessByName(String name) {
        return mBusinessDtos
                .stream()
                .filter(businessDto -> businessDto
                        .getName()
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
