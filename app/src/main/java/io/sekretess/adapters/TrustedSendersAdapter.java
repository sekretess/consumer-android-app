package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private final Context context;

    public TrustedSendersAdapter(Context context, List<TrustedSender> mTrustedSenders) {
        this.context = context;
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
        try {
            if (!trustedSender.getBusinessName().equals("Add New")) {
                String businessImageFilePath = context.getFilesDir().getPath() + "/images/"
                        + trustedSender.getBusinessName() + ".jpeg";
                Bitmap bitmap = BitmapFactory.decodeFile(businessImageFilePath);
                holder.getImgTrustedSender().setImageBitmap(bitmap);
            } else {
                holder.getImgTrustedSender().setImageResource(R.drawable.round_add_moderator_24);
                if (trustedSender.getOnClickListener() != null) {
                    holder.getImgTrustedSender().setOnClickListener(trustedSender.getOnClickListener());
                }
            }
        } catch (Exception e) {
            Log.e("TrustedSendersAdapter", "Error loading image: " + e.getMessage(), e);
        }

    }

    @Override
    public int getItemCount() {
        return mTrustedSenders.size();
    }
}
