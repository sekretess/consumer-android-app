package io.sekretess.dto;

import android.view.View;

public class TrustedSender {
    private final String businessName;

    private final View.OnClickListener onClickListener;

    public TrustedSender(String businessName, View.OnClickListener onClickListener) {
        this.businessName = businessName;
        this.onClickListener = onClickListener;
    }

    public TrustedSender(String businessName){
        this.businessName = businessName;
        this.onClickListener = null;
    }

    public String getBusinessName() {
        return businessName;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
