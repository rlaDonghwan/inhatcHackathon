package com.example.hackathonproject.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hackathonproject.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private Context context;  // 현재 컨텍스트를 저장하는 변수
    private List<Chat> chatList;  // 채팅 목록을 저장하는 리스트
    private int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 변수

    // ChatListAdapter 생성자: 컨텍스트, 채팅 목록, 로그인된 사용자 ID를 받아 초기화
    public ChatListAdapter(Context context, List<Chat> chatList, int loggedInUserId) {
        this.context = context;
        this.chatList = chatList;
        this.loggedInUserId = loggedInUserId;
    }

    @Override
    public int getCount() {
        return chatList.size();  // 채팅 목록의 크기를 반환
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);  // 해당 위치의 채팅 아이템을 반환
    }

    @Override
    public long getItemId(int position) {
        return chatList.get(position).getChatID();  // 해당 위치의 채팅 ID를 반환
    }

    // 각 채팅 아이템의 뷰를 설정하는 메서드
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // convertView가 null인 경우, 새로운 뷰를 생성하고 ViewHolder를 초기화
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
            holder = new ViewHolder();
            holder.otherUserName = convertView.findViewById(R.id.otherUserName);  // 상대방 이름 텍스트뷰
            holder.lastMessage = convertView.findViewById(R.id.lastMessage);  // 마지막 메시지 텍스트뷰
            holder.lastMessageTime = convertView.findViewById(R.id.lastMessageTime);  // 마지막 메시지 시간 텍스트뷰
            holder.newMessageIcon = convertView.findViewById(R.id.newMessageIcon);  // 새 메시지 아이콘
            convertView.setTag(holder);  // ViewHolder를 태그로 설정
        } else {
            holder = (ViewHolder) convertView.getTag();  // convertView가 null이 아닌 경우, 기존의 ViewHolder 사용
        }

        // 현재 위치의 채팅 데이터를 가져옴
        Chat chat = chatList.get(position);

        // 상대방의 이름을 설정
        holder.otherUserName.setText(chat.getOtherUserName());

        // 마지막 메시지 내용을 설정
        holder.lastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");

        // 마지막 메시지 시간을 현재 시간과 비교하여 포맷
        String formattedTime = formatTimeAgo(chat.getLastMessageTime());
        holder.lastMessageTime.setText(formattedTime);

        // 새 메시지가 있는지 여부에 따라 아이콘 표시 여부 설정
        holder.newMessageIcon.setVisibility(chat.isNewMessage() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    // 메시지 시간을 현재 시간과 비교하여 포맷팅하는 메서드
    private String formatTimeAgo(String lastMessageTime) {
        try {
            // ISO_LOCAL_DATE_TIME 형식의 시간을 파싱
            LocalDateTime dateTime = LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // KST 시간대로 변환 후 "오후 5:20" 형식으로 변환하여 반환
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return lastMessageTime;  // 파싱에 실패한 경우 원본 문자열 반환
        }
    }

    // 뷰 홀더 클래스: 뷰의 각 요소를 재사용하기 위해 사용
    static class ViewHolder {
        TextView otherUserName;  // 상대방 이름 텍스트뷰
        TextView lastMessage;  // 마지막 메시지 텍스트뷰
        TextView lastMessageTime;  // 마지막 메시지 시간 텍스트뷰
        ImageView newMessageIcon;  // 새 메시지 아이콘
    }
}
