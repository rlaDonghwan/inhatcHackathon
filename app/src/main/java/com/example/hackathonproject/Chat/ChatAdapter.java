package com.example.hackathonproject.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;

    public ChatAdapter(List<ChatItem> chatList) {
        this.chatList = chatList;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatItem chatItem = chatList.get(position);
        holder.postTitle.setText(chatItem.getTitle());
        holder.postDetails.setText(chatItem.getLastMessage());
        holder.postLocation.setText(chatItem.getTimestamp());
        // 새로운 메시지가 있을 경우 아이콘 표시
        holder.newMessageIcon.setVisibility(chatItem.isNewMessage() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle, postDetails, postLocation;
        ImageView newMessageIcon;

        ChatViewHolder(View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            newMessageIcon = itemView.findViewById(R.id.newMessageIcon);
        }
    }
}
