package com.sekretess.view.holders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;

public class SubscrubedBusinessesViewHolder extends RecyclerView.ViewHolder {

    public final TextView txtSubscBusinessName;
    public final Button btnUnsubscribe;

    public SubscrubedBusinessesViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtSubscBusinessName = itemView.findViewById(R.id.txtSubscBusinessName);
        this.btnUnsubscribe = itemView.findViewById(R.id.btnUnsubscribe);
    }
}
