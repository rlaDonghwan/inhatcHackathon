package com.example.hackathonproject.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
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

    private List<ChatMessage> messages;  // 채팅 메시지 리스트를 저장하는 변수
    private final int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 변수

    // ChatAdapter 생성자: 로그인된 사용자 ID를 받아와서 초기화
    public ChatAdapter(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        Log.d("ChatAdapter", "LoggedInUserId in ChatAdapter constructor: " + loggedInUserId);  // 로그 추가
    }

    // 메시지 리스트를 설정하고 어댑터 갱신
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();  // 데이터가 변경되었음을 어댑터에 알림
    }

    // 뷰 홀더를 생성하는 메서드
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);  // 뷰 홀더 객체를 생성하여 반환
    }

    // 뷰 홀더에 데이터를 바인딩하는 메서드
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);  // 해당 위치의 메시지 가져오기

        // 두 컨테이너 모두 초기화하여 안보이도록 설정
        holder.myMessageContainer.setVisibility(View.GONE);
        holder.otherMessageContainer.setVisibility(View.GONE);

        // SharedPreferences에서 폰트 크기 가져오기
        SharedPreferences preferences = holder.itemView.getContext().getSharedPreferences("fontSizePrefs", Context.MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 폰트 크기를 적용
        holder.myMessageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        holder.otherMessageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        holder.myMessageTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        holder.otherMessageTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);

        if (message.getSenderUserId() == loggedInUserId) {
            // 내가 보낸 메시지인 경우
            holder.myMessageContainer.setVisibility(View.VISIBLE);  // 내 메시지 컨테이너 보이도록 설정
            holder.myMessageTextView.setText(message.getMessageText());  // 내 메시지 내용 설정
            holder.myMessageTime.setText(message.getFormattedTime());  // 내 메시지 시간 설정
        } else {
            // 상대방이 보낸 메시지인 경우
            holder.otherMessageContainer.setVisibility(View.VISIBLE);  // 상대 메시지 컨테이너 보이도록 설정
            holder.otherMessageTextView.setText(message.getMessageText());  // 상대 메시지 내용 설정
            holder.otherMessageTime.setText(message.getFormattedTime());  // 상대 메시지 시간 설정
        }

        // 로그로 현재 위치와 메시지 정보를 출력
        Log.d("ChatAdapter", "Position: " + position + ", Message: " + message.getMessageText() + ", Sender: " + message.getSenderUserId() + ", LoggedInUser: " + loggedInUserId);
    }

    // 아이템의 개수를 반환하는 메서드
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;  // 메시지가 null이 아니면 메시지 개수 반환, null이면 0 반환
    }

    // RecyclerView의 뷰 홀더 클래스
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout myMessageContainer;  // 내 메시지 컨테이너
        LinearLayout otherMessageContainer;  // 상대 메시지 컨테이너
        TextView myMessageTextView;  // 내 메시지 텍스트 뷰
        TextView otherMessageTextView;  // 상대 메시지 텍스트 뷰
        TextView myMessageTime;  // 내 메시지 시간 텍스트 뷰
        TextView otherMessageTime;  // 상대 메시지 시간 텍스트 뷰

        // MessageViewHolder 생성자: 뷰를 초기화
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // 각 뷰 요소를 아이템 레이아웃에서 가져와 초기화
            myMessageContainer = itemView.findViewById(R.id.myMessageContainer);
            otherMessageContainer = itemView.findViewById(R.id.otherMessageContainer);
            myMessageTextView = itemView.findViewById(R.id.msgContent2);
            otherMessageTextView = itemView.findViewById(R.id.msgContent1);
            myMessageTime = itemView.findViewById(R.id.msgTime2);
            otherMessageTime = itemView.findViewById(R.id.msgTime1);
        }
    }
}
