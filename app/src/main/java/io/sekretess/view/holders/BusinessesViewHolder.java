package io.sekretess.view.holders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;

public class BusinessesViewHolder extends RecyclerView.ViewHolder {

    private final TextView txtBusinessName;
    private final ImageView imgBusiness;
    private final TextView txtSubscriptionStatus;

    public BusinessesViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtBusinessName = itemView.findViewById(R.id.txtBusinessName);
        this.imgBusiness = itemView.findViewById(R.id.imgBusiness);
        this.txtSubscriptionStatus = itemView.findViewById(R.id.txtSubscriptionStatus);
    }


    public ImageView getImgBusiness() {
        return imgBusiness;
    }

    public TextView getTxtBusinessName() {
        return txtBusinessName;
    }

    public TextView getTxtSubscriptionStatus() {
        return txtSubscriptionStatus;
    }
}
