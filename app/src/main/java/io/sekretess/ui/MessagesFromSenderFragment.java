package io.sekretess.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.adapters.MessageAdapter;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.MessageRecordDto;

import java.util.List;

public class MessagesFromSenderFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private String from;
    private List<MessageRecordDto> messages;


    @Override
    public void onDestroy() {
        super.onDestroy();
        SekretessDependencyProvider.messageEventStream().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SekretessDependencyProvider.messageEventStream().observe(getViewLifecycleOwner(), event -> {
            Log.i("MessageFromSenderFragment", "new-incoming-message event received");
            messages = SekretessDependencyProvider.messageService().loadMessages(from);
            messageAdapter = new MessageAdapter(messages);
            recyclerView.setAdapter(messageAdapter);
            messageAdapter.notifyItemInserted(messages.size());
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        from = getArguments().getString("from");
        recyclerView = view.findViewById(R.id.messages_rv);
        messages = SekretessDependencyProvider.messageService().loadMessages(from);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                                @NonNull RecyclerView.ViewHolder viewHolder) {
                        return makeMovementFlags(0, ItemTouchHelper.END);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        SekretessDependencyProvider.messageService().deleteMessage(messages.get(position).getMessageId());
                        messages.remove(position);
                        messageAdapter.notifyItemRemoved(position);
                        messageAdapter.notifyItemRangeChanged(position, messages.size());
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }


    public void setMessages(List<MessageRecordDto> messages) {
        this.messages = messages;
    }
}