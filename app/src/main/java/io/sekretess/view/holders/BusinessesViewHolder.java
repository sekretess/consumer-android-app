package io.sekretess.view.holders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;

public class BusinessesViewHolder extends RecyclerView.ViewHolder {

    private final TextView txtBusinessName;
    private final Button btnSubscribe;
    private final ImageView imgBusiness;

    public BusinessesViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtBusinessName = itemView.findViewById(R.id.txtBusinessName);
        this.btnSubscribe = itemView.findViewById(R.id.btnSubscribe);
        this.imgBusiness = itemView.findViewById(R.id.imgBusiness);
    }

    public Button getBtnSubscribe() {
        return btnSubscribe;
    }

    public ImageView getImgBusiness() {
        return imgBusiness;
    }

    public TextView getTxtBusinessName() {
        return txtBusinessName;
    }
}
