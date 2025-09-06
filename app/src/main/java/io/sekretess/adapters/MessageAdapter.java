package io.sekretess.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.ItemType;
import io.sekretess.view.holders.ConcreteChatCustomViewHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<ConcreteChatCustomViewHolder> {

    private final List<MessageRecordDto> messages;

    private String currentGroupingDate = "";

    public MessageAdapter(List<MessageRecordDto> messages) {
        this.messages = messages;
    }


    @Override
    public int getItemViewType(int position) {
        MessageRecordDto messageRecordDto = messages.get(position);
        if (messageRecordDto.getItemType() == ItemType.HEADER) return 1;
        return 0;
    }

    @NonNull
    @Override
    public ConcreteChatCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View chatsView = layoutInflater.inflate(R.layout.message_from_other, parent, false);
        return new ConcreteChatCustomViewHolder(chatsView);
    }


    @Override
    public void onBindViewHolder(@NonNull ConcreteChatCustomViewHolder holder, int position) {
        MessageRecordDto messageRecordDto = messages.get(position);
        holder.getMessageText().setText(messageRecordDto.getMessage());
        LocalDateTime messageDateTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(messageRecordDto.getMessageDate()),
                        ZoneId.systemDefault());

        if(holder.getItemViewType() == 1) {
            holder.getMessageDate().setText(messageRecordDto.getDateText());
            holder.getMessageDate().setVisibility(View.VISIBLE);
        }else {

                holder.getMessageDate().setVisibility(View.GONE);
            }

        holder.getMessageText().setText(messageRecordDto.getMessage());
        holder.getMessageTime().setText(DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(messageDateTime));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


}
