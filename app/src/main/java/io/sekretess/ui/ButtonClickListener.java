package io.sekretess.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

import net.openid.appauth.AuthState;

public class ButtonClickListener implements View.OnClickListener {

    private final String businessName;

    public ButtonClickListener(String businessName) {
        this.businessName = businessName;
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        Button button = (Button) v;
        String buttonText = button.getText().toString();
        if (buttonText.equalsIgnoreCase("unsubscribe")) {
            boolean result = ApiClient.unSubscribeFromBusiness(context, businessName);
            if (result) button.setText("Subscribe");
        } else if (buttonText.equalsIgnoreCase("subscribe")) {
            boolean result = ApiClient.subscribeToBusiness(context, businessName);
            if (result) button.setText("UnSubscribe");
        }
    }
}
