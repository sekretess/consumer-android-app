package io.sekretess.ui;

import static android.content.Context.RECEIVER_EXPORTED;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.Constants;
import io.sekretess.MainActivity;
import io.sekretess.R;
import io.sekretess.adapters.SendersAdapter;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.repository.DbHelper;

import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View fragmentView;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ChatsFragment", "new-incoming-message event received");
            List<MessageBriefDto> messageBriefs = DbHelper.getInstance(context).getMessageBriefs();
            SendersAdapter sendersAdapter = updateAdapter();
            recyclerView.setAdapter(sendersAdapter);
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
        recyclerView = fragmentView.findViewById(R.id.chat);
        recyclerView.setAdapter(updateAdapter());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return fragmentView;
    }


    private SendersAdapter updateAdapter(){
        List<MessageBriefDto> messageBriefs = DbHelper.getInstance(getContext()).getMessageBriefs();
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