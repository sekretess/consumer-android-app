package com.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;

public class ConcreteChatCustomViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText;
    public TextView messageDate;
    public TextView messageTime;

    public ConcreteChatCustomViewHolder(@NonNull View itemView) {
        super(itemView);
        this.messageText = itemView.findViewById(R.id.messageText);
        this.messageDate = itemView.findViewById(R.id.text_gchat_date_me);
        this.messageTime = itemView.findViewById(R.id.text_gchat_timestamp_me);
    }
}
