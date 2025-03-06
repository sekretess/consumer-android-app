package com.sekretess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;
import com.sekretess.dto.BusinessDto;
import com.sekretess.repository.DbHelper;
import com.sekretess.ui.ButtonClickListener;
import com.sekretess.view.holders.BusinessesViewHolder;

import java.util.List;

public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesViewHolder> {

    private DbHelper dbHelper;
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
        dbHelper = DbHelper.getInstance(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View subscriptionView = layoutInflater.inflate(R.layout.businesses_layout, parent, false);
        return new BusinessesViewHolder(subscriptionView);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessesViewHolder holder, int position) {
        BusinessDto businessDto = mBusinessDtos.get(position);
        holder.txtBusinessName.setText(businessDto.getBusinessName());
        if (mSubscribedBusinesses.contains(businessDto.getBusinessName())) {
            holder.btnSubscribe.setText("Unsubscribe");
        } else {
            holder.btnSubscribe.setText("Subscribe");
        }

        holder.btnSubscribe
                .setOnClickListener(new ButtonClickListener(businessDto.getBusinessName()));
    }


    @Override
    public int getItemCount() {
        return mBusinessDtos.size();
    }


}
