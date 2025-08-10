package io.sekretess.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.adapters.MessageAdapter;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.repository.DbHelper;

import java.util.List;

public class MessagesFromSenderFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        String from = getArguments().getString("from");
        recyclerView = view.findViewById(R.id.messages_rv);
        List<MessageRecordDto> messages = DbHelper.getInstance(getContext()).loadMessages(from);
        messages.add(new MessageRecordDto("budbee", "This is a Message",
                1754648569000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message1",
                1751970169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message2",
                1751970169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message3",
                1751970169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message4",
                1751970169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message5",
                1754562169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message6",
                1723026169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message7",
                1723026169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message8",
                1723026169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message9",
                1723026169000L));
        messages.add(new MessageRecordDto("budbee", "This is a Message10",
                1723026169000L));
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }


}