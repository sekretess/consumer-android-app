package io.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;

public class ConcreteChatCustomViewHolder extends RecyclerView.ViewHolder {
    private TextView messageText;
    private TextView messageDate;
    private TextView messageTime;

    public ConcreteChatCustomViewHolder(@NonNull View itemView) {
        super(itemView);
        this.messageText = itemView.findViewById(R.id.messageText);
        this.messageDate = itemView.findViewById(R.id.txtMessageDate);
        this.messageTime = itemView.findViewById(R.id.text_gchat_timestamp_me);
    }

    public TextView getMessageDate() {
        return messageDate;
    }

    public TextView getMessageText() {
        return messageText;
    }

    public TextView getMessageTime() {
        return messageTime;
    }
}
