package io.sekretess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.view.holders.ConcreteChatCustomViewHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<ConcreteChatCustomViewHolder> {

    private final List<MessageRecordDto> messages;
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private String currentGroupingDate = "";

    public MessageAdapter(List<MessageRecordDto> messages) {
        this.messages = messages;
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
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

        String dateTimeText = dateTimeText(messageDateTime);

        if (!currentGroupingDate.equals(dateTimeText)) {
            currentGroupingDate = dateTimeText;
            holder.getMessageDate().setText(dateTimeText);
            holder.getMessageDate().setVisibility(View.VISIBLE);
        } else {
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

    public static String dateTimeText(LocalDateTime dateTime) {
        LocalDateTime today = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(dateTime, today);
        long monthsBetween = ChronoUnit.MONTHS.between(dateTime, today);

        if (daysBetween == 0) {
            return "Today";
        }
        if (daysBetween <= 7) {
            return WEEK_FORMATTER.format(dateTime);
        } else if (monthsBetween >= 12) {
            return YEAR_FORMATTER.format(dateTime);
        } else {
            return MONTH_FORMATTER.format(dateTime);
        }
    }
}
