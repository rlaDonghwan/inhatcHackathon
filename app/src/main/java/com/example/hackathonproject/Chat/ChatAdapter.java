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
    private int otherUserId; // 상대방의 사용자 ID

    public ChatAdapter(int loggedInUserId, int otherUserId) { // 상대방 ID를 추가로 받습니다.
        this.loggedInUserId = loggedInUserId;
        this.otherUserId = otherUserId; // 초기화
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
            // 내가 보낸 메시지
            holder.myMessageTextView.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setVisibility(View.GONE);
            holder.myMessageTextView.setText(message.getMessageText());
        } else if (message.getSenderUserId() == otherUserId) {
            // 상대방이 보낸 메시지
            holder.myMessageTextView.setVisibility(View.GONE);
            holder.otherMessageTextView.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setText(message.getMessageText());
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
