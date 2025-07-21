package io.sekretess.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.sekretess.R;
import io.sekretess.dto.TrustedSender;
import io.sekretess.view.holders.TrustedSenderHolder;

public class TrustedSendersAdapter extends RecyclerView.Adapter<TrustedSenderHolder> {
    private List<TrustedSender> mTrustedSenders;

    public TrustedSendersAdapter(List<TrustedSender> mTrustedSenders) {
        this.mTrustedSenders = mTrustedSenders;
    }

    @NonNull
    @Override
    public TrustedSenderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View trustedSenderView = layoutInflater.inflate(R.layout.trusted_sender_layout, parent, false);
        return new TrustedSenderHolder(trustedSenderView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrustedSenderHolder holder, int position) {
        TrustedSender trustedSender = mTrustedSenders.get(position);
        holder.getTxtTrustedSender().setText(trustedSender.getBusinessName());
        if (trustedSender.getIcon() != null && !trustedSender.getIcon().isEmpty()) {
            try {
                Picasso.get().load(trustedSender.getIcon()).into(holder.getImgTrustedSender());
            } catch (Exception e) {
                Log.e("TrustedSendersAdapter", "Error loading image: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mTrustedSenders.size();
    }
}
