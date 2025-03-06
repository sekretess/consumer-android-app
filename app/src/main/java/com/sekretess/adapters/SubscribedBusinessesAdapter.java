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
import com.sekretess.view.holders.SubscrubedBusinessesViewHolder;

import java.util.List;

public class SubscribedBusinessesAdapter extends RecyclerView.Adapter<SubscrubedBusinessesViewHolder> {

    private DbHelper dbHelper;

    private final List<String> mSubscribedBusinesses;

    public SubscribedBusinessesAdapter( List<String> subscribedBusinesses) {

        this.mSubscribedBusinesses = subscribedBusinesses;
    }

    @NonNull
    @Override
    public SubscrubedBusinessesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        dbHelper = DbHelper.getInstance(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View subscriptionView = layoutInflater.inflate(R.layout.subscribed_businesses_layout, parent, false);
        return new SubscrubedBusinessesViewHolder(subscriptionView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscrubedBusinessesViewHolder holder, int position) {
        String subscribedBusinessName = mSubscribedBusinesses.get(position);
        holder.txtSubscBusinessName.setText(subscribedBusinessName);


        holder.btnUnsubscribe
                .setOnClickListener(new ButtonClickListener(subscribedBusinessName));
    }


    @Override
    public int getItemCount() {
        return mSubscribedBusinesses.size();
    }


}
