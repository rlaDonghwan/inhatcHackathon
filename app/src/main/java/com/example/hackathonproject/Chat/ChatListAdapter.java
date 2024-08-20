package com.example.hackathonproject.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hackathonproject.R;

import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private Context context;
    private List<Chat> chatList;
    private int loggedInUserId;

    public ChatListAdapter(Context context, List<Chat> chatList, int loggedInUserId) {
        this.context = context;
        this.chatList = chatList;
        this.loggedInUserId = loggedInUserId;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return chatList.get(position).getChatID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
            holder = new ViewHolder();
            holder.otherUserName = convertView.findViewById(R.id.otherUserName);
            holder.lastMessage = convertView.findViewById(R.id.lastMessage);
            holder.lastMessageTime = convertView.findViewById(R.id.lastMessageTime);
            holder.newMessageIcon = convertView.findViewById(R.id.newMessageIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Chat chat = chatList.get(position);

        // 상대방의 이름 설정
        holder.otherUserName.setText(chat.getOtherUserName());

        // 마지막 메시지 내용 설정
        holder.lastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");

        // 마지막 메시지 시간 설정
        holder.lastMessageTime.setText(chat.getLastMessageTime() != null ? chat.getLastMessageTime() : "");

        // 새 메시지가 있는지 여부에 따라 아이콘 표시
        holder.newMessageIcon.setVisibility(chat.isNewMessage() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    static class ViewHolder {
        TextView otherUserName;
        TextView lastMessage;
        TextView lastMessageTime;
        ImageView newMessageIcon;
    }
}
