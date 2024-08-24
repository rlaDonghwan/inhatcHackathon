package com.example.hackathonproject.Chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatMessage> messages;
    private final int loggedInUserId;

    public ChatAdapter(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        Log.d("ChatAdapter", "LoggedInUserId in ChatAdapter constructor: " + loggedInUserId);  // 로그 추가
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

        // 두 컨테이너 모두 초기화
        holder.myMessageContainer.setVisibility(View.GONE);
        holder.otherMessageContainer.setVisibility(View.GONE);

        if (message.getSenderUserId() == loggedInUserId) {
            // 내가 보낸 메시지
            holder.myMessageContainer.setVisibility(View.VISIBLE);
            holder.myMessageTextView.setText(message.getMessageText());
            holder.myMessageTime.setText(message.getFormattedTime());
        } else {
            // 상대방이 보낸 메시지
            holder.otherMessageContainer.setVisibility(View.VISIBLE);
            holder.otherMessageTextView.setText(message.getMessageText());
            holder.otherMessageTime.setText(message.getFormattedTime());
        }

        // 로그 추가
        Log.d("ChatAdapter", "Position: " + position + ", Message: " + message.getMessageText() + ", Sender: " + message.getSenderUserId() + ", LoggedInUser: " + loggedInUserId);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout myMessageContainer;
        LinearLayout otherMessageContainer;
        TextView myMessageTextView;
        TextView otherMessageTextView;
        TextView myMessageTime;
        TextView otherMessageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            myMessageContainer = itemView.findViewById(R.id.myMessageContainer);
            otherMessageContainer = itemView.findViewById(R.id.otherMessageContainer);
            myMessageTextView = itemView.findViewById(R.id.msgContent2);
            otherMessageTextView = itemView.findViewById(R.id.msgContent1);
            myMessageTime = itemView.findViewById(R.id.msgTime2);
            otherMessageTime = itemView.findViewById(R.id.msgTime1);
        }
    }
}
