package com.sekretess.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;


public class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView txtSenderName ;
    public TextView txtMessageCount;
    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);

       this.txtSenderName = (TextView) itemView.findViewById(R.id.txtSenderName);
       this.txtMessageCount = (TextView) itemView.findViewById(R.id.txtMessageCount);

    }
}
