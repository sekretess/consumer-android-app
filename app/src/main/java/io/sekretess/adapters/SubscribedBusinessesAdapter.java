package io.sekretess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;

import io.sekretess.repository.DbHelper;
import io.sekretess.ui.ButtonClickListener;
import io.sekretess.view.holders.SubscrubedBusinessesViewHolder;

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
        holder.getTxtSubscBusinessName().setText(subscribedBusinessName);


        holder.getBtnUnsubscribe()
                .setOnClickListener(new ButtonClickListener(subscribedBusinessName));
    }


    @Override
    public int getItemCount() {
        return mSubscribedBusinesses.size();
    }


}
