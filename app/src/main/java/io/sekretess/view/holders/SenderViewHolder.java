package io.sekretess.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import io.sekretess.R;


public class SenderViewHolder extends RecyclerView.ViewHolder {
    private TextView txtSenderName;
//    private TextView txtMessageCount;

    private ShapeableImageView imgBusinessIcon;

    public SenderViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtSenderName = itemView.findViewById(R.id.txtSenderName);
//        this.txtMessageCount = itemView.findViewById(R.id.txtMessageCount);
        this.imgBusinessIcon = itemView.findViewById(R.id.imgBusinessIcon);
    }

//    public TextView getTxtMessageCount() {
//        return txtMessageCount;
//    }

    public TextView getTxtSenderName() {
        return txtSenderName;
    }

    public ShapeableImageView getImgBusinessIcon() {
        return imgBusinessIcon;
    }
}
