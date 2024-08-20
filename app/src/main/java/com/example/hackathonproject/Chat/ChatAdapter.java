package com.example.hackathonproject.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.ChatMessageDAO;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatMessage> messages;
    private int loggedInUserId;

    public ChatAdapter(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        this.messages = new ArrayList<>(); // 메시지 리스트 초기화
    }

    public void setMessages(List<ChatMessage> messages) {
        if (messages != null) {
            this.messages = messages;
            notifyDataSetChanged(); // 데이터 변경 시 어댑터에 알림
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

        // 메시지의 송신자가 로그인한 사용자일 경우
        if (message.getSenderUserId() == loggedInUserId) {
            holder.myMessageTextView.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setVisibility(View.GONE);
            holder.myMessageTextView.setText(message.getMessageText());
        } else {
            // 다른 사용자가 보낸 메시지일 경우
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

        TextView myMessageTextView;
        TextView otherMessageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            myMessageTextView = itemView.findViewById(R.id.msgContent1);
            otherMessageTextView = itemView.findViewById(R.id.msgContent2);
        }
    }
}
