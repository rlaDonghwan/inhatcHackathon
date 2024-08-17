package com.example.hackathonproject.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.ChatMessageDAO;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatMessage> messages;
    private int loggedInUserId;

    public ChatAdapter(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (message.getSenderUserId() == loggedInUserId) {
            holder.myMessageTextView.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setVisibility(View.GONE);
            holder.myMessageTextView.setText(message.getMessageText());
        } else {
            holder.myMessageTextView.setVisibility(View.GONE);
            holder.otherMessageTextView.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setText(message.getMessageText());
        }
    }


    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView myMessageTextView;
        TextView otherMessageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            myMessageTextView = itemView.findViewById(R.id.msgContent1);
            otherMessageTextView = itemView.findViewById(R.id.msgContent2);
        }
    }
}
