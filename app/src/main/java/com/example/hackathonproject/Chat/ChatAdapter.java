package com.example.hackathonproject.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatMessage> messages;
    private int loggedInUserId;

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
            // 로그인한 사용자가 보낸 메시지
            holder.myMessageContainer.setVisibility(View.VISIBLE);
            holder.otherMessageContainer.setVisibility(View.GONE);

            holder.myMessageTextView.setText(message.getMessageText());
            holder.myMessageTimeTextView.setText(message.getFormattedTime());
        } else {
            // 상대방이 보낸 메시지
            holder.myMessageContainer.setVisibility(View.GONE);
            holder.otherMessageContainer.setVisibility(View.VISIBLE);

            holder.otherMessageTextView.setText(message.getMessageText());
            holder.otherMessageTimeTextView.setText(message.getFormattedTime());
        }
    }

    @Override
    public int getItemCount() {
        return (messages != null) ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout myMessageContainer;
        LinearLayout otherMessageContainer;
        TextView myMessageTextView;
        TextView myMessageTimeTextView;
        TextView otherMessageTextView;
        TextView otherMessageTimeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            myMessageContainer = itemView.findViewById(R.id.myMessageContainer);
            otherMessageContainer = itemView.findViewById(R.id.otherMessageContainer);
            myMessageTextView = itemView.findViewById(R.id.msgContent2);
            myMessageTimeTextView = itemView.findViewById(R.id.msgTime2);
            otherMessageTextView = itemView.findViewById(R.id.msgContent1);
            otherMessageTimeTextView = itemView.findViewById(R.id.msgTime1);
        }
    }
}
