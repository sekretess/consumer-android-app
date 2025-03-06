package com.sekretess.ui;

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

import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.adapters.SendersAdapter;
import com.sekretess.dto.MessageBriefDto;
import com.sekretess.repository.DbHelper;

import java.util.List;

public class ChatsFragment extends Fragment {
    private SendersAdapter sendersAdapter;
    private RecyclerView recyclerView;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsActivity", "new-incoming-message event received");
            List<MessageBriefDto> messageBriefs = DbHelper.getInstance(context).getMessageBriefs();
            sendersAdapter = new SendersAdapter(messageBriefs);
            recyclerView.setAdapter(sendersAdapter);
            sendersAdapter.notifyItemInserted(messageBriefs.size());
        }
    };

    private final BroadcastReceiver refreshTokenFailedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatActivity", "Refresh token failed event received");
            startActivity(new Intent(ChatsFragment.this.getContext(), LoginActivity.class));
            ChatsFragment.this.getActivity().finishActivity(1);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chats,container, false);
        recyclerView = view.findViewById(R.id.chat);
        List<MessageBriefDto> messageBriefs = DbHelper.getInstance(getContext()).getMessageBriefs();
        sendersAdapter = new SendersAdapter(messageBriefs);
        recyclerView.setAdapter(sendersAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshTokenFailedBroadcastReceiver,
                new IntentFilter(Constants.EVENT_REFRESH_TOKEN_FAILED));
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