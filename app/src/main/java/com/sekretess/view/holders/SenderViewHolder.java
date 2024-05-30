package com.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;


public class SenderViewHolder extends RecyclerView.ViewHolder {
    public TextView txtSenderName ;
    public TextView txtMessageCount;
    public SenderViewHolder(@NonNull View itemView) {
        super(itemView);

       this.txtSenderName = itemView.findViewById(R.id.txtSenderName);
       this.txtMessageCount = itemView.findViewById(R.id.txtMessageCount);

    }
}
