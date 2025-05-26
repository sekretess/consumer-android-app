package io.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.adapters.SendersAdapter;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.repository.DbHelper;

import java.util.List;

public class ChatsFragment extends Fragment {
    private SendersAdapter sendersAdapter;
    private RecyclerView recyclerView;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsActivity", "new-incoming-message event received");
            List<MessageBriefDto> messageBriefs = DbHelper.getInstance(context).getMessageBriefs();
            sendersAdapter = new SendersAdapter(messageBriefs, getParentFragmentManager());
            recyclerView.setAdapter(sendersAdapter);
            sendersAdapter.notifyItemInserted(messageBriefs.size());
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chats, container, false);
        recyclerView = view.findViewById(R.id.chat);
        List<MessageBriefDto> messageBriefs = DbHelper.getInstance(getContext()).getMessageBriefs();
        sendersAdapter = new SendersAdapter(messageBriefs, getParentFragmentManager());
        recyclerView.setAdapter(sendersAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.EVENT_NEW_INCOMING_MESSAGE));
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
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}