package com.sekretess.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;
import com.sekretess.dto.BusinessDto;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.ui.ButtonClickListener;
import com.sekretess.utils.ApiClient;
import com.sekretess.view.holders.SubscriptionViewHolder;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionViewHolder> {

    private DbHelper dbHelper;
    private final List<BusinessDto> mBusinessDtos;
    private final List<String> mSubscribedBusinesses;

    public SubscriptionAdapter(List<BusinessDto> mBusinessDtos, List<String> subscribedBusinesses) {
        this.mBusinessDtos = mBusinessDtos;
        this.mSubscribedBusinesses = subscribedBusinesses;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        dbHelper = new DbHelper(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View subscriptionView = layoutInflater.inflate(R.layout.subscription_layout, parent, false);
        return new SubscriptionViewHolder(subscriptionView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
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
