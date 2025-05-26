package io.sekretess.adapters;

import android.content.Context;
import android.content.Intent;
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
    private final FragmentManager fragmentManager;
    private final List<MessageBriefDto> mMessageBriefs;

    public SendersAdapter(List<MessageBriefDto> mMessageBriefs, FragmentManager fragmentManager) {
        this.mMessageBriefs = mMessageBriefs;
        this.fragmentManager = fragmentManager;
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
        holder.getTxtSenderName().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("from", holder.getTxtSenderName().getText().toString());
            MessagesFromSenderFragment fragment = new MessagesFromSenderFragment();
            fragment.setArguments(bundle);
            replaceFragment(fragment);
        });
        holder.getTxtMessageCount().setText(String.valueOf(messageBriefDto.getCount()));
    }

    @Override
    public int getItemCount() {
        return mMessageBriefs.size();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
