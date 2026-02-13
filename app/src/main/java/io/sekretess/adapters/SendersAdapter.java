package io.sekretess.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.view.holders.SenderViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SendersAdapter extends RecyclerView.Adapter<SenderViewHolder> implements Filterable {

    private final List<MessageBriefDto> mMessageBriefs;
    private final List<MessageBriefDto> filteredList = new ArrayList<>();
    private final ItemClickListener listener;
    private final Context context;
    private boolean filtered = false;

    public SendersAdapter(Context context, List<MessageBriefDto> mMessageBriefs, ItemClickListener listener) {
        this.context = context;
        this.mMessageBriefs = mMessageBriefs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View sendersView = layoutInflater.inflate(R.layout.senders_layout, parent, false);
        return new SenderViewHolder(sendersView);
    }


    @Override
    public void onBindViewHolder(@NonNull SenderViewHolder holder, int position) {
        List<MessageBriefDto> source = findSource();
        if (position >= source.size())
            return;
        MessageBriefDto messageBriefDto = source.get(position);
        holder.getTxtSenderName().setText(messageBriefDto.getSender());
        holder.getMessageBriefLayout()
                .setOnClickListener(v -> listener
                        .onClick(holder.getTxtSenderName().getText().toString()));
        String businessImageFilePath = context.getFilesDir().getPath() + "/images/"
                + messageBriefDto.getSender() + ".jpeg";
        Bitmap bitmap = BitmapFactory.decodeFile(businessImageFilePath);
        holder.getImgBusinessIcon().setImageBitmap(bitmap);
        String messageText = messageBriefDto
                .getMessageText()
                .substring(0, Math.min(130, messageBriefDto.getMessageText().length()));
        if (messageText.length() < messageBriefDto.getMessageText().length()) {
            messageText = messageText.concat("...");
        }

        holder.getTxtMessageBrief().setText(messageText);
    }

    private List<MessageBriefDto> findSource() {
        return filtered ? filteredList : mMessageBriefs;
    }

    @Override
    public int getItemCount() {
        List<MessageBriefDto> source = findSource();
        return source == null ? 0 : source.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<MessageBriefDto> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtered = false;
                    results.values = mMessageBriefs;
                    return results;
                } else {
                    filtered = true;
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (MessageBriefDto messageBriefDto : mMessageBriefs) {
                        if (messageBriefDto.getSender().toLowerCase().contains(filterPattern)
                                || messageBriefDto.getMessageText().toLowerCase().contains(filterPattern)) {
                            filteredList.add(messageBriefDto);
                        }
                    }
                    results.values = filteredList;
                    return results;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List<MessageBriefDto>) results.values);
                notifyDataSetChanged();
            }
        };
    }


    public interface ItemClickListener {
        void onClick(String senderName);
    }
}
