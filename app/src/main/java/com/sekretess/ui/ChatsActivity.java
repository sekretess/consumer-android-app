package com.sekretess.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ViewGroup;

import com.sekretess.R;

import java.util.Arrays;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chat);
        List<String> senders = Arrays.asList("BankSweden","Facebook","Netflix");
        SendersAdapter sendersAdapter = new SendersAdapter(senders);
        recyclerView.setAdapter(sendersAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}