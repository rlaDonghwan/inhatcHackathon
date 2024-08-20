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

    private ListView chatListView;
    private List<Chat> chatList;
    private ChatDAO chatDAO;
    private ChatListAdapter chatListAdapter;
    private int loggedInUserId;
    private SwipeRefreshLayout swipeRefreshLayout; // 스와이프 새로고침 레이아웃 변수 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chat_list_view);

        // SwipeRefreshLayout 초기화
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 새로고침 시 채팅 목록 다시 로드
            new LoadChatListTask(new DatabaseConnection()).execute();
        });

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        // DatabaseConnection 객체 생성
        DatabaseConnection databaseConnection = new DatabaseConnection();

        // 비동기적으로 데이터베이스 연결 및 채팅 목록 로드
        new LoadChatListTask(databaseConnection).execute();

        // 필터 버튼 초기 선택 상태 설정
        TextView filterAllButton = findViewById(R.id.button_all);
        filterAllButton.setSelected(true);
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
            Chat selectedChat = chatList.get(position);
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", selectedChat.getChatID()); // 선택한 채팅의 ID
            intent.putExtra("otherUserId", selectedChat.getOtherUserID(loggedInUserId)); // 선택한 채팅의 상대방 ID
            intent.putExtra("postId", selectedChat.getPostID()); // 선택한 채팅의 게시글 ID (만약 필요하다면)
            intent.putExtra("currentUserId", loggedInUserId); // 현재 사용자 ID 전달

            // 로그 추가
            Log.d("ChatListActivity", "Opening chat with chatId: " + selectedChat.getChatID() +
                    " otherUserId: " + selectedChat.getOtherUserID(loggedInUserId) +
                    " postId: " + selectedChat.getPostID() +
                    " currentUserId: " + loggedInUserId);

            startActivity(intent);
        });



    }

    private class LoadChatListTask extends AsyncTask<Void, Void, List<Chat>> {
        private DatabaseConnection databaseConnection;
        private Connection connection;

        public LoadChatListTask(DatabaseConnection databaseConnection) {
            this.databaseConnection = databaseConnection;
        }

        @Override
        protected List<Chat> doInBackground(Void... voids) {
            Log.d("LoadChatListTask", "doInBackground started");
            List<Chat> chatList = null;
            try {
                connection = databaseConnection.connect();
                if (connection != null) {
                    chatDAO = new ChatDAO(connection);
                    chatList = chatDAO.getAllChatsForUser(loggedInUserId);
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
            swipeRefreshLayout.setRefreshing(false); // 새로고침 완료
            if (chats != null && !chats.isEmpty()) {
                chatList = chats;
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, loggedInUserId);
                chatListView.setAdapter(chatListAdapter);
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
        // 필터 버튼 클릭 시 선택된 버튼을 강조하고 필터를 설정하는 메소드
    private void setFilter(TextView selectedButton, String filterType) {
        resetFilterButtons();
        selectedButton.setSelected(true);
        // 여기서 선택된 필터 유형에 따라 채팅 목록을 필터링하는 로직을 추가합니다.
    }

    // 모든 필터 버튼의 선택 상태를 초기화하는 메소드
    private void resetFilterButtons() {
        TextView filterAllButton = findViewById(R.id.button_all);
        TextView filterSellButton = findViewById(R.id.button_sell);
        TextView filterBuyButton = findViewById(R.id.button_buy);

        filterAllButton.setSelected(false);
        filterSellButton.setSelected(false);
        filterBuyButton.setSelected(false);
    }
}
