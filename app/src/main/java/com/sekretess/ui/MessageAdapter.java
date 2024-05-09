package com.sekretess.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sekretess.R;
import com.sekretess.dto.MessageRecordDto;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<ConcreteChatCustomViewHolder> {

    private List<MessageRecordDto> messages;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

    public MessageAdapter(List<MessageRecordDto> messages) {
        this.messages = messages;
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
        holder.messageText.setText(messageRecordDto.getMessage());

        holder.messageDate.setText(DateTimeFormatter.ofPattern("MMMM dd").withZone(ZoneId.systemDefault()).format(dateTimeFormatter.parse(messageRecordDto.getMessageDate())));
        holder.messageTime.setText(DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(dateTimeFormatter.parse(messageRecordDto.getMessageDate())));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
