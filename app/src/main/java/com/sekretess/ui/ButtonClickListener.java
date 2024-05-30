package com.sekretess.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.sekretess.dto.jwt.Jwt;
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.ApiClient;

public class ButtonClickListener implements View.OnClickListener {

    private final String businessName;

    public ButtonClickListener(String businessName) {
        this.businessName = businessName;
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        DbHelper dbHelper = new DbHelper(context);
        Jwt jwt = dbHelper.getJwt();
        Button button = (Button) v;
        String buttonText = button.getText().toString();
        if(buttonText.equalsIgnoreCase("unsubscribe")) {
            boolean result = ApiClient.unSubscribeFromBusiness(businessName, jwt.getIdToken().getToken());
            if(result) button.setText("Subscribe");
        }else if(buttonText.equalsIgnoreCase("subscribe")){
            boolean result = ApiClient.subscribeToBusiness(businessName, jwt.getIdToken().getToken());
            if(result) button.setText("UnSubscribe");
        }
    }
}
