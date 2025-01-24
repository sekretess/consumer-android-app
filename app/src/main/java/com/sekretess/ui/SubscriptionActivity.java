package com.sekretess.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.sekretess.adapters.SubscriptionAdapter;
import com.sekretess.R;
import com.sekretess.dto.BusinessDto;
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.ApiClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SubscriptionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
        setContentView(R.layout.activity_subscription);
        recyclerView = findViewById(R.id.subscriptionsRecycler);
        List<BusinessDto> businessList = ApiClient.getBusinesses()
                .stream()
                .map(BusinessDto::new)
                .collect(Collectors.toList());
        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(dbHelper.getAuthState().getIdToken());
        SubscriptionAdapter subscriptionAdapter =
                new SubscriptionAdapter(businessList, subscribedBusinesses);
        recyclerView.setAdapter(subscriptionAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}