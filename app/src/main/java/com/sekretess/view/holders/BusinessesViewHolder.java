package com.sekretess.view.holders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;

public class BusinessesViewHolder extends RecyclerView.ViewHolder {

    public final TextView txtBusinessName;
    public final Button btnSubscribe;

    public BusinessesViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtBusinessName = itemView.findViewById(R.id.txtBusinessName);
        this.btnSubscribe = itemView.findViewById(R.id.btnSubscribe);
    }
}
