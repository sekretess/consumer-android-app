package com.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.Constants;
import com.sekretess.MainActivity;
import com.sekretess.R;
import com.sekretess.dto.KeyMaterial;
import com.sekretess.dto.MessageBriefDto;
import com.sekretess.model.MessageEntity;
import com.sekretess.repository.DbHelper;
import com.sekretess.service.RefreshTokenService;
import com.sekretess.utils.KeycloakManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ChatsActivity extends AppCompatActivity {
    private SendersAdapter sendersAdapter;
    private RecyclerView recyclerView;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<MessageBriefDto> messageBriefs = getMessageBriefs();
            sendersAdapter = new SendersAdapter(messageBriefs);
            recyclerView.setAdapter(sendersAdapter);
        }
    };

    private BroadcastReceiver refreshTokenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(new Intent(ChatsActivity.this, MainActivity.class));
            ChatsActivity.this.finishActivity(1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        recyclerView = (RecyclerView) findViewById(R.id.chat);
        List<MessageBriefDto> messageBriefs = getMessageBriefs();
        sendersAdapter = new SendersAdapter(messageBriefs);
        recyclerView.setAdapter(sendersAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        startService(new Intent(this, RefreshTokenService.class));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("new-incoming-message"));
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshTokenBroadcastReceiver,
                new IntentFilter("refresh-token-failed"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh_token) {
            broadcastUpdateKeyEvent();
        }
        return true;
    }

    private List<MessageBriefDto> getMessageBriefs() {
        Cursor resultCursor = new DbHelper(this).getReadableDatabase().query(MessageEntity.TABLE_NAME,
                new String[]{MessageEntity.COLUMN_SENDER, "COUNT(" + MessageEntity.COLUMN_SENDER + ") AS count"},
                null,
                null,
                MessageEntity.COLUMN_SENDER,
                null,
                null
        );

        List<MessageBriefDto> resultArray = new ArrayList<>();

        while (resultCursor.moveToNext()) {
            String senderName = resultCursor.getString(0);
            int messageCount = resultCursor.getInt(1);
            resultArray.add(new MessageBriefDto(senderName, messageCount));

        }
        return resultArray;
    }

    private void broadcastUpdateKeyEvent() {
        Intent intent = new Intent("update-key-event");
        LocalBroadcastManager.getInstance(ChatsActivity.this).sendBroadcast(intent);
    }
}