package io.sekretess.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.SearchAutoComplete;
import androidx.appcompat.widget.Toolbar;

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

import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private RecyclerView messagesRecycleView;
    private SendersAdapter sendersAdapter;
    private View fragmentView;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsFragment", "new-incoming-message event received");
            String username = new DbHelper(context).getUserNameFromJwt();
            //List<MessageBriefDto> messageBriefs = new DbHelper(context).getMessageBriefs(username);
            sendersAdapter = updateMessageAdapter(context);
            messagesRecycleView.setAdapter(sendersAdapter);
            //sendersAdapter.notifyItemInserted(messageBriefs.size());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO Mock data - remove on production
        ContextCompat.registerReceiver(getContext(), broadcastReceiver,
                new IntentFilter(Constants.EVENT_NEW_INCOMING_MESSAGE), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_home, container, false);
        renderTrustedSendersRecycleView();
        renderMessagesRecycleView();
        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setTitle("Home");
        SearchView searchView = fragmentView.findViewById(R.id.searchView);
        prepareSearchView(searchView);
        return fragmentView;
    }

    private void prepareSearchView(SearchView searchView) {
        SearchAutoComplete viewById = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        viewById.setTextColor(Color.WHITE);

        // To change magnifier icon color
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(Color.WHITE);
        // To change close button icon color
        ImageView searchCloseIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchCloseIcon.setColorFilter(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                sendersAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void renderTrustedSendersRecycleView() {
        RecyclerView trustedSendersRecycleView = fragmentView.findViewById(R.id.trusted_senders);
        trustedSendersRecycleView.setAdapter(updateTrustedSendersAdapter());
        trustedSendersRecycleView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
    }


    private void renderMessagesRecycleView() {
        messagesRecycleView = fragmentView.findViewById(R.id.chat);
        sendersAdapter = updateMessageAdapter(getContext());
        messagesRecycleView.setAdapter(sendersAdapter);
        messagesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private TrustedSendersAdapter updateTrustedSendersAdapter() {
        DbHelper dbHelper = new DbHelper(getContext());
        List<TrustedSender> trustedSenders = dbHelper.getTopSenders()
                .stream()
                .distinct()
                .map(businessName -> new TrustedSender(businessName))
                .collect(Collectors.toList());
        trustedSenders.add(new TrustedSender("Add New", v -> {
            AppCompatActivity activity = (AppCompatActivity) fragmentView.getContext();
            BusinessesFragment fragment = new BusinessesFragment();
            activity.getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.frame_layout, fragment).
                    commit();
        }));
        return new TrustedSendersAdapter(getContext(), trustedSenders);
    }


    private SendersAdapter updateMessageAdapter(Context context) {
        try (DbHelper db = new DbHelper(context)) {
            String username = db.getUserNameFromJwt();
            List<MessageBriefDto> messageBriefs = db.getMessageBriefs(username);
            return new SendersAdapter(getContext(), messageBriefs, (sender) -> {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("from", sender);
                    AppCompatActivity activity = (AppCompatActivity) fragmentView.getContext();
                    MessagesFromSenderFragment fragment = new MessagesFromSenderFragment();
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
                    Toolbar toolbar = activity.findViewById(R.id.my_toolbar);
                    toolbar.setTitle(sender);
                } catch (Exception e) {
                    Log.e("ChatsFragment", "Error", e);
                }
            });
        }
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