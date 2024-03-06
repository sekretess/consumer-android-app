package com.sekretess.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;

import java.util.List;

public class SendersAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private List<String> mSenders ;

    public SendersAdapter(List<String> mSenders){
        this.mSenders = mSenders;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View sendersView = layoutInflater.inflate(R.layout.senders_layout, parent, false);

        return new CustomViewHolder(sendersView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.txtSenderName.setText(mSenders.get(position));
    }

    @Override
    public int getItemCount() {
        return mSenders.size();
    }
}
