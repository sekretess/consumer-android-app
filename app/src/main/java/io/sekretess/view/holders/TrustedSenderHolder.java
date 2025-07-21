package io.sekretess.view.holders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;

public class TrustedSenderHolder extends RecyclerView.ViewHolder {
    private final ImageButton imgTrustedSender;
    private final TextView txtTrustedSender;


    public TrustedSenderHolder(@NonNull View itemView) {
        super(itemView);
        this.imgTrustedSender = itemView.findViewById(R.id.img_trusted_sender);
        this.txtTrustedSender = itemView.findViewById(R.id.txt_trusted_sender);
    }

    public ImageButton getImgTrustedSender() {
        return imgTrustedSender;
    }

    public TextView getTxtTrustedSender() {
        return txtTrustedSender;
    }
}
