package com.example.hackathonproject.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages;
    private int loggedInUserId;
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    public ChatAdapter(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        this.messages = new ArrayList<>();
    }

    public void setMessages(List<ChatMessage> messages) {
        if (messages != null) {
            this.messages = messages;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getSenderUserId() == loggedInUserId) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).bind(message);
        } else {
            ((OtherMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {

        TextView myMessageTextView;
        TextView myMessageTimeTextView;

        MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            myMessageTextView = itemView.findViewById(R.id.msgContent2);
            myMessageTimeTextView = itemView.findViewById(R.id.msgTime2);
        }

        void bind(ChatMessage message) {
            myMessageTextView.setText(message.getMessageText());
            myMessageTimeTextView.setText(message.getSentTime().toString());
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {

        TextView otherMessageTextView;
        TextView otherMessageTimeTextView;

        OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            otherMessageTextView = itemView.findViewById(R.id.msgContent1);
            otherMessageTimeTextView = itemView.findViewById(R.id.msgTime1);
        }

        void bind(ChatMessage message) {
            otherMessageTextView.setText(message.getMessageText());
            otherMessageTimeTextView.setText(message.getSentTime().toString());
        }
    }
}
