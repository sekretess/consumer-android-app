package io.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;


public class SenderViewHolder extends RecyclerView.ViewHolder {
    private TextView txtSenderName ;
    private TextView txtMessageCount;
    public SenderViewHolder(@NonNull View itemView) {
        super(itemView);
       this.txtSenderName = itemView.findViewById(R.id.txtSenderName);
       this.txtMessageCount = itemView.findViewById(R.id.txtMessageCount);
    }

    public TextView getTxtMessageCount() {
        return txtMessageCount;
    }

    public TextView getTxtSenderName() {
        return txtSenderName;
    }
}
