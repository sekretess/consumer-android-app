package com.sekretess.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;
import com.sekretess.dto.MessageBriefDto;
import com.sekretess.view.holders.SenderViewHolder;
import com.sekretess.ui.MessagesFromSenderActivity;

import java.util.List;

public class SendersAdapter extends RecyclerView.Adapter<SenderViewHolder> {
    private final List<MessageBriefDto> mMessageBriefs;

    public SendersAdapter(List<MessageBriefDto> mMessageBriefs) {
        this.mMessageBriefs = mMessageBriefs;
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
        holder.txtSenderName.setText(messageBriefDto.getSender());
        holder.txtSenderName.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MessagesFromSenderActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("from", holder.txtSenderName.getText().toString());
            intent.putExtras(bundle);
            v.getContext().startActivity(intent);
        });
        holder.txtMessageCount.setText(String.valueOf(messageBriefDto.getCount()));
    }

    @Override
    public int getItemCount() {
        return mMessageBriefs.size();
    }
}
