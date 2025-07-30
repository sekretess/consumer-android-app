package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.view.holders.SenderViewHolder;
import io.sekretess.ui.MessagesFromSenderFragment;

import java.util.List;

public class SendersAdapter extends RecyclerView.Adapter<SenderViewHolder> {

    private final List<MessageBriefDto> mMessageBriefs;
    private final ItemClickListener listener;
    private final Context context;

    public SendersAdapter(Context context, List<MessageBriefDto> mMessageBriefs, ItemClickListener listener) {
        this.context = context;
        this.mMessageBriefs = mMessageBriefs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View sendersView = layoutInflater.inflate(R.layout.senders_layout, parent, false);
        return new SenderViewHolder(sendersView);
    }


    @Override
    public void onBindViewHolder(@NonNull SenderViewHolder holder, int position) {
        MessageBriefDto messageBriefDto = mMessageBriefs.get(position);
        holder.getTxtSenderName().setText(messageBriefDto.getSender());
        holder.getTxtSenderName().setOnClickListener(v -> listener.onClick(holder.getTxtSenderName().getText().toString()));
//        holder.getTxtMessageCount().setText(String.valueOf(messageBriefDto.getCount()));
        String businessImageFilePath = context.getFilesDir().getPath() + "/images/"
                + messageBriefDto.getSender() + ".jpeg";
        Bitmap bitmap = BitmapFactory.decodeFile(businessImageFilePath);
        holder.getImgBusinessIcon().setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return mMessageBriefs.size();
    }


    public interface ItemClickListener {
        void onClick(String senderName);
    }
}
