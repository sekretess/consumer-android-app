package com.sekretess.ui;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.MessageRecordDto;
import com.sekretess.model.MessageStoreEntity;
import com.sekretess.repository.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class MessagesFromSenderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String from = getIntent().getStringExtra("from");
        setContentView(R.layout.chat_layout);
        recyclerView = (RecyclerView) findViewById(R.id.messages_rv);
        List<MessageRecordDto> messages = getMessages(from);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private List<MessageRecordDto> getMessages(String from) {
        Cursor resultCursor = new DbHelper(this).getReadableDatabase(Constants.password)
                .query(MessageStoreEntity.TABLE_NAME, new String[]{MessageStoreEntity.COLUMN_SENDER,
                                MessageStoreEntity.COLUMN_MESSAGE_BODY,
                                MessageStoreEntity.COLUMN_CREATED_AT
                        },
                        "sender=?",
                        new String[]{from}, null, null, null);
        List<MessageRecordDto> resultArray = new ArrayList<>();

        while (resultCursor.moveToNext()) {
            String sender = resultCursor.getString(0);
            String messageBody = resultCursor.getString(1);
            String createdAt = resultCursor.getString(2);

            resultArray.add(new MessageRecordDto(sender, messageBody, createdAt));
        }

        return resultArray;
    }
}