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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.adapters.SendersAdapter;
import io.sekretess.adapters.TrustedSendersAdapter;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.TrustedSender;
import io.sekretess.repository.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView messagesRecycleView;
    private View fragmentView;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsFragment", "new-incoming-message event received");
            List<MessageBriefDto> messageBriefs = DbHelper.getInstance(context).getMessageBriefs();
            SendersAdapter sendersAdapter = updateMessageAdapter();
            messagesRecycleView.setAdapter(sendersAdapter);
            sendersAdapter.notifyItemInserted(messageBriefs.size());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ChatsActivity", "Registering receiver. Context: " + getActivity().getApplicationContext());
        Log.i("ChatsActivity", "Registering receiver. Context: " + getActivity().getBaseContext());
        Log.i("ChatsActivity", "Registering receiver. Context: " + getContext());
        ContextCompat.registerReceiver(getContext(), broadcastReceiver,
                new IntentFilter(Constants.EVENT_NEW_INCOMING_MESSAGE), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_chats, container, false);
        renderTrustedSendersRecycleView();
        renderMessagesRecycleView();

        return fragmentView;
    }

    private void renderTrustedSendersRecycleView() {
        RecyclerView trustedSendersRecycleView = fragmentView.findViewById(R.id.trusted_senders);
        trustedSendersRecycleView.setAdapter(updateTrustedSendersAdapter());
        trustedSendersRecycleView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
    }


    private void renderMessagesRecycleView() {
        messagesRecycleView = fragmentView.findViewById(R.id.chat);
        messagesRecycleView.setAdapter(updateMessageAdapter());
        messagesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private TrustedSendersAdapter updateTrustedSendersAdapter() {
        List<TrustedSender> trustedSenders = new ArrayList<>();
        trustedSenders.add(new TrustedSender("Starbugs", "https://logonoid.com/images/starbucks-logo.png"));
        trustedSenders.add(new TrustedSender("Mitsubishi", "https://wieck-mmna-production.s3.amazonaws.com/photos/d05f8e2530ea21931994bf23f5237e24afb0fff1/preview-928x522.jpg"));
        trustedSenders.add(new TrustedSender("Add New", R.drawable.round_add_moderator_24));
        return new TrustedSendersAdapter(trustedSenders);
    }


    private SendersAdapter updateMessageAdapter() {
        List<MessageBriefDto> messageBriefs = DbHelper.getInstance(getContext()).getMessageBriefs();
        messageBriefs.add(new MessageBriefDto("Sweden Bank", 3));
        messageBriefs.add(new MessageBriefDto("ABC Bank", 1));
        messageBriefs.add(new MessageBriefDto("TeliaSonera", 1));
        return new SendersAdapter(messageBriefs, (sender) -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("from", sender);
                AppCompatActivity activity = (AppCompatActivity) fragmentView.getContext();
                MessagesFromSenderFragment fragment = new MessagesFromSenderFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
            } catch (Exception e) {
                Log.e("ChatsFragment", "Error", e);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }
}