package com.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.Constants;
import com.sekretess.MainActivity;
import com.sekretess.R;
import com.sekretess.dto.MessageBriefDto;
import com.sekretess.repository.DbHelper;

import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private SendersAdapter sendersAdapter;
    private RecyclerView recyclerView;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsActivity", "new-incoming-message event received");
            List<MessageBriefDto> messageBriefs = new DbHelper(context).getMessageBriefs();
            sendersAdapter = new SendersAdapter(messageBriefs);
            recyclerView.setAdapter(sendersAdapter);
            sendersAdapter.notifyItemInserted(messageBriefs.size());
        }
    };

    private BroadcastReceiver refreshTokenFailedBroadcastReceiver = new BroadcastReceiver() {
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
        List<MessageBriefDto> messageBriefs = new DbHelper(getApplicationContext()).getMessageBriefs();
        sendersAdapter = new SendersAdapter(messageBriefs);
        recyclerView.setAdapter(sendersAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
//        startService(new Intent(this, RefreshTokenService.class));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(refreshTokenFailedBroadcastReceiver,
                new IntentFilter(Constants.EVENT_REFRESH_TOKEN_FAILED), RECEIVER_EXPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.EVENT_NEW_INCOMING_MESSAGE), RECEIVER_EXPORTED);
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


    private void broadcastUpdateKeyEvent() {
        Intent intent = new Intent(Constants.EVENT_UPDATE_KEY);
        sendBroadcast(intent);
    }
}