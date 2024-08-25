package com.example.hackathonproject.Chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Seminar.SeminarActivity;
import com.example.hackathonproject.Setting.SettingsActivity;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.Login.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // 이 라인을 추가

public class ChatListActivity extends AppCompatActivity {

    private ListView chatListView;  // 채팅 목록을 표시할 ListView
    private List<Chat> chatList;  // 채팅 목록을 저장하는 리스트
    private ChatDAO chatDAO;  // 데이터베이스에서 채팅 정보를 가져오기 위한 DAO 객체
    private ChatListAdapter chatListAdapter;  // 채팅 목록을 표시하기 위한 어댑터
    private int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 변수
    private SwipeRefreshLayout swipeRefreshLayout; // 스와이프 새로고침 레이아웃 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chat_list_view);  // ListView 초기화

        // SwipeRefreshLayout 초기화 및 새로고침 리스너 설정
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 새로고침 시 채팅 목록 다시 로드
            new LoadChatListTask(new DatabaseConnection()).execute();
        });

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();  // 세션에서 로그인된 사용자 ID를 가져옴

        // DatabaseConnection 객체 생성
        DatabaseConnection databaseConnection = new DatabaseConnection();

        // 비동기적으로 데이터베이스 연결 및 채팅 목록 로드
        new LoadChatListTask(databaseConnection).execute();

        // 필터 버튼 초기 선택 상태 설정
        TextView filterAllButton = findViewById(R.id.button_all);
        filterAllButton.setSelected(true);  // "전체" 버튼을 기본 선택 상태로 설정
        TextView filterSellButton = findViewById(R.id.button_sell);
        TextView filterBuyButton = findViewById(R.id.button_buy);

        // 필터 버튼 클릭 이벤트 설정
        filterAllButton.setOnClickListener(view -> setFilter(filterAllButton, "전체"));
        filterSellButton.setOnClickListener(view -> setFilter(filterSellButton, "판매"));
        filterBuyButton.setOnClickListener(view -> setFilter(filterBuyButton, "구매"));

        // 교육 받기 탭 클릭 시 EducationActivity로 이동
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        // 강연자 신청 탭 클릭 시 SeminarActivity로 이동
        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, SeminarActivity.class);
            startActivity(intent);
        });

        // 채팅 탭 클릭 시 ChatListActivity로 이동 (현재 액티비티와 동일)
        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            // 현재 화면이므로 새로 액티비티를 시작할 필요는 없습니다.
        });

        // 설정 탭 클릭 시 SettingsActivity로 이동
        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // ListView 클릭 이벤트 설정
        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            Chat selectedChat = chatList.get(position);  // 선택된 채팅 아이템 가져오기
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", selectedChat.getChatID()); // 선택한 채팅의 ID 전달
            intent.putExtra("otherUserId", selectedChat.getOtherUserID(loggedInUserId)); // 선택한 채팅의 상대방 ID 전달
            intent.putExtra("postId", selectedChat.getPostID()); // 선택한 채팅의 게시글 ID 전달 (필요 시)
            intent.putExtra("currentUserId", loggedInUserId); // 현재 사용자 ID 전달

            // 로그로 채팅 정보를 출력
            Log.d("ChatListActivity", "Opening chat with chatId: " + selectedChat.getChatID() +
                    " otherUserId: " + selectedChat.getOtherUserID(loggedInUserId) +
                    " postId: " + selectedChat.getPostID() +
                    " currentUserId: " + loggedInUserId);

            startActivity(intent);  // ChatActivity 시작
        });
    }

    // 비동기로 채팅 목록을 로드하는 AsyncTask 클래스
    private class LoadChatListTask extends AsyncTask<Void, Void, List<Chat>> {
        private DatabaseConnection databaseConnection;
        private Connection connection;

        public LoadChatListTask(DatabaseConnection databaseConnection) {
            this.databaseConnection = databaseConnection;  // 생성자에서 DatabaseConnection 객체를 받아옴
        }

        @Override
        protected List<Chat> doInBackground(Void... voids) {
            Log.d("LoadChatListTask", "doInBackground started");
            List<Chat> chatList = null;
            try {
                connection = databaseConnection.connect();  // 데이터베이스 연결 시도
                if (connection != null) {
                    chatDAO = new ChatDAO(connection);
                    chatList = chatDAO.getAllChatsForUser(loggedInUserId);  // 로그인된 사용자의 모든 채팅 목록을 가져옴
                }
            } catch (SQLException e) {
                Log.e("LoadChatListTask", "SQLException: " + e.getMessage());
                e.printStackTrace();
            }
            return chatList;
        }

        @Override
        protected void onPostExecute(List<Chat> chats) {
            Log.d("LoadChatListTask", "onPostExecute started");
            swipeRefreshLayout.setRefreshing(false); // 새로고침 완료 상태로 설정
            if (chats != null && !chats.isEmpty()) {
                chatList = chats;  // 채팅 목록을 클래스 변수에 저장
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, loggedInUserId);  // 어댑터 초기화
                chatListView.setAdapter(chatListAdapter);  // ListView에 어댑터 설정
                Log.d("LoadChatListTask", "Chat list loaded successfully with " + chats.size() + " items.");
            } else {
                Log.e("LoadChatListTask", "Failed to load chat list or chat list is empty.");
                if (chats == null) {
                    Log.e("LoadChatListTask", "Chats list is null.");
                } else {
                    Log.e("LoadChatListTask", "Chats list is empty.");
                }
            }
        }
    }

    // 필터 버튼 클릭 시 선택된 버튼을 강조하고 필터를 설정하는 메서드
    private void setFilter(TextView selectedButton, String filterType) {
        resetFilterButtons();  // 모든 필터 버튼의 선택 상태를 초기화
        selectedButton.setSelected(true);  // 선택된 버튼을 강조
        // 여기서 선택된 필터 유형에 따라 채팅 목록을 필터링하는 로직을 추가합니다.
    }

    // 모든 필터 버튼의 선택 상태를 초기화하는 메서드
    private void resetFilterButtons() {
        TextView filterAllButton = findViewById(R.id.button_all);
        TextView filterSellButton = findViewById(R.id.button_sell);
        TextView filterBuyButton = findViewById(R.id.button_buy);

        filterAllButton.setSelected(false);  // "전체" 버튼 선택 해제
        filterSellButton.setSelected(false);  // "판매" 버튼 선택 해제
        filterBuyButton.setSelected(false);  // "구매" 버튼 선택 해제
    }
}
