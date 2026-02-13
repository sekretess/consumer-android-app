package io.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import io.sekretess.R;


public class SenderViewHolder extends RecyclerView.ViewHolder {
    private TextView txtSenderName;
    private TextView txtMessageBrief   ;
    private ConstraintLayout messageBriefLayout;

    private ShapeableImageView imgBusinessIcon;

    public SenderViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtSenderName = itemView.findViewById(R.id.txtSenderName);
        this.txtMessageBrief = itemView.findViewById(R.id.txtBriefBody);
        this.imgBusinessIcon = itemView.findViewById(R.id.imgBusinessIcon);
        this.messageBriefLayout = itemView.findViewById(R.id.messageBriefLayout);
    }

    public TextView getTxtMessageBrief() {
        return txtMessageBrief;
    }

    public TextView getTxtSenderName() {
        return txtSenderName;
    }

    public ShapeableImageView getImgBusinessIcon() {
        return imgBusinessIcon;
    }

    public ConstraintLayout getMessageBriefLayout() {
        return messageBriefLayout;
    }
}
