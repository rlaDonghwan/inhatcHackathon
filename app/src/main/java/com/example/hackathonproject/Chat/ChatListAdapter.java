package com.example.hackathonproject.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
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

    private final Context context;  // 현재 컨텍스트를 저장하는 변수
    private List<Chat> chatList;  // 채팅 목록을 저장하는 리스트
    private final int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 변수

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

        // SharedPreferences에서 폰트 크기 가져오기
        SharedPreferences preferences = context.getSharedPreferences("fontSizePrefs", Context.MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // convertView가 null인 경우, 새로운 뷰를 생성하고 ViewHolder를 초기화
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
            holder = new ViewHolder();
            holder.otherUserName = convertView.findViewById(R.id.otherUserName);  // 상대방 이름 텍스트뷰
            holder.lastMessage = convertView.findViewById(R.id.lastMessage);  // 마지막 메시지 텍스트뷰
            holder.lastMessageTime = convertView.findViewById(R.id.lastMessageTime);  // 마지막 메시지 시간 텍스트뷰
            holder.newMessageIcon = convertView.findViewById(R.id.newMessageIcon);  // 새 메시지 아이콘

            // 가져온 폰트 크기를 텍스트뷰에 적용
            holder.otherUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
            holder.lastMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
            holder.lastMessageTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);

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
        holder.lastMessageTime.setText(formatTimeAgo(chat.getLastMessageTime()));

        // 메시지의 읽음 상태에 따라 새 메시지 아이콘 표시 여부 설정
        if (chat.getAuthorID() == loggedInUserId) {
            // 내가 작성한 메시지라면 상대방이 읽지 않은 경우에만 아이콘 표시
            holder.newMessageIcon.setVisibility(chat.isAuthorMessageRead() ? View.GONE : View.VISIBLE);
        } else {
            // 상대방이 작성한 메시지라면 내가 읽지 않은 경우에만 아이콘 표시
            holder.newMessageIcon.setVisibility(chat.isOtherUserMessageRead() ? View.GONE : View.VISIBLE);
        }

        return convertView;
    }

    // 메시지 시간을 현재 시간과 비교하여 포맷팅하는 메서드
    private String formatTimeAgo(String lastMessageTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a h:mm");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, a h:mm");

            if (dateTime.toLocalDate().equals(now.toLocalDate())) {
                return dateTime.format(timeFormatter);  // 같은 날이면 시간만 표시
            } else {
                return dateTime.format(dateFormatter);  // 날짜와 시간을 함께 표시
            }
        } catch (Exception e) {
            e.printStackTrace();
            return lastMessageTime;  // 파싱에 실패한 경우 원본 문자열 반환
        }
    }

    // 어댑터의 데이터셋을 업데이트하는 메서드
    public void updateChatList(List<Chat> newChatList) {
        this.chatList = newChatList;  // 새로운 채팅 목록으로 업데이트
        notifyDataSetChanged();  // 데이터셋 변경을 알림
    }

    // ViewHolder 클래스에 있는 필드를 확인
    static class ViewHolder {
        TextView otherUserName;  // 상대방 이름 텍스트뷰
        TextView lastMessage;  // 마지막 메시지 텍스트뷰
        TextView lastMessageTime;  // 마지막 메시지 시간 텍스트뷰
        ImageView newMessageIcon;  // 새 메시지 아이콘
    }
}