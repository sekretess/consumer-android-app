package io.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.adapters.MessageAdapter;
import io.sekretess.adapters.SendersAdapter;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.repository.DbHelper;

import java.util.List;

public class MessagesFromSenderFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private String from;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MessageFromSenderFragment", "new-incoming-message event received");
            List<MessageRecordDto> messages = DbHelper.getInstance(context).loadMessages(from);
            messageAdapter = new MessageAdapter(messages);
            recyclerView.setAdapter(messageAdapter);
            messageAdapter.notifyItemInserted(messages.size()-1);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextCompat.registerReceiver(getContext(), broadcastReceiver,
                new IntentFilter(Constants.EVENT_NEW_INCOMING_MESSAGE), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        from = getArguments().getString("from");
        recyclerView = view.findViewById(R.id.messages_rv);
        List<MessageRecordDto> messages = DbHelper.getInstance(getContext()).loadMessages(from);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }


}