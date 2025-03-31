package io.sekretess.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.adapters.MessageAdapter;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.repository.DbHelper;

import java.util.List;

public class MessagesFromSenderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String from = getIntent().getStringExtra("from");
        setContentView(R.layout.chat_layout);
        recyclerView = findViewById(R.id.messages_rv);
        List<MessageRecordDto> messages = DbHelper.getInstance(this).loadMessages(from);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}