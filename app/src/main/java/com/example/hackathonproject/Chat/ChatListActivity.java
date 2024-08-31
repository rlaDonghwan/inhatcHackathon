package com.example.hackathonproject.Chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Lecture.LectureActivity;
import com.example.hackathonproject.Setting.SettingsActivity;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.Login.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";
    private ListView chatListView;  // 채팅 목록을 표시할 ListView
    private List<Chat> chatList;  // 전체 채팅 목록을 저장하는 리스트
    private List<Chat> filteredChatList;  // 필터링된 채팅 목록을 저장하는 리스트
    private ChatDAO chatDAO;  // 데이터베이스에서 채팅 정보를 가져오기 위한 DAO 객체
    private ChatListAdapter chatListAdapter;  // 채팅 목록을 표시하기 위한 어댑터
    private int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 변수
    private SwipeRefreshLayout swipeRefreshLayout; // 스와이프 새로고침 레이아웃 변수

    private TextView filterAllButton;
    private TextView filterEducationButton;
    private TextView filterLectureButton;

    private Runnable refreshRunnable;
    private Handler handler;  // Handler for periodic updates
    private Connection connection;

    private static final String CHANNEL_ID = "chat_notification_channel";

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
        filterAllButton = findViewById(R.id.button_all);
        filterAllButton.setSelected(true);  // "전체" 버튼을 기본 선택 상태로 설정
        filterEducationButton = findViewById(R.id.button_education);
        filterLectureButton = findViewById(R.id.button_lecture);

        // 필터 버튼 클릭 이벤트 설정
        filterAllButton.setOnClickListener(view -> setFilter(filterAllButton, "전체"));
        filterEducationButton.setOnClickListener(view -> setFilter(filterEducationButton, "교육"));
        filterLectureButton.setOnClickListener(view -> setFilter(filterLectureButton, "강연"));

        // 교육 받기 탭 클릭 시 EducationActivity로 이동
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        // 강연자 신청 탭 클릭 시 SeminarActivity로 이동
        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, LectureActivity.class);
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

        // ChatListActivity에서 ChatActivity를 시작하는 코드
        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            Chat selectedChat = filteredChatList.get(position);

            // 현재 사용자가 Author인지 OtherUser인지에 따라 읽음 상태를 업데이트
            if (selectedChat.getAuthorID() == loggedInUserId) {
                selectedChat.setOtherUserMessageRead(true);
            } else {
                selectedChat.setAuthorMessageRead(true);
            }

            // ChatActivity로 이동
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", selectedChat.getChatID());
            intent.putExtra("otherUserId", selectedChat.getCounterpartUserID(loggedInUserId));
            intent.putExtra("educationID", selectedChat.getEducationID());
            intent.putExtra("lectureID", selectedChat.getLectureID());
            startActivityForResult(intent, 1);
        });

        // Handler와 Runnable 설정
        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                new LoadChatListTask(new DatabaseConnection()).execute(); // 채팅 목록을 주기적으로 로드합니다.
                handler.postDelayed(this, 1000); // 1초마다 반복 실행
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChatList();  // 새로 고침 함수 호출
        handler.post(refreshRunnable); // Runnable 실행 시작
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable); // 화면이 보이지 않으면 새로고침을 중지
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 채팅방 삭제 후 돌아왔을 때 목록을 새로 고침
            refreshChatList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable); // 반복 실행 중지
        }
    }

// 나머지 코드 그대로 유지

    private void refreshChatList() {
        swipeRefreshLayout.setRefreshing(true);
        new LoadChatListTask(new DatabaseConnection()).execute();
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
                chatList = chats;  // 전체 채팅 목록을 클래스 변수에 저장
                filteredChatList = new ArrayList<>(chatList);  // 필터링된 목록을 전체 목록으로 초기화
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, filteredChatList, loggedInUserId);  // 어댑터 초기화
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 필터 버튼 클릭 시 선택된 버튼을 강조하고 필터를 설정하는 메서드
    private void setFilter(TextView selectedButton, String filterType) {
        resetFilterButtons();  // 모든 필터 버튼의 선택 상태를 초기화
        selectedButton.setSelected(true);  // 선택된 버튼을 강조

        // 선택된 버튼에 대해 'round_button_background_selected.xml'을 배경으로 설정
        selectedButton.setBackgroundResource(R.drawable.round_button_background_selected);

        // 필터링 로직
        if (chatList == null || chatList.isEmpty()) {
            Log.e("ChatListActivity", "채팅 목록이 없습니다.");
            filteredChatList = new ArrayList<>();  // 빈 목록으로 초기화
        } else {
            if (filterType.equals("전체")) {
                filteredChatList = new ArrayList<>(chatList);  // 전체 채팅을 표시
            } else if (filterType.equals("교육")) {
                filteredChatList = new ArrayList<>();
                for (Chat chat : chatList) {
                    if (chat.getEducationID() != null && chat.getEducationID() > 0) {
                        filteredChatList.add(chat);  // 교육 ID가 있는 채팅만 추가
                    }
                }
            } else if (filterType.equals("강연")) {
                filteredChatList = new ArrayList<>();
                for (Chat chat : chatList) {
                    if (chat.getLectureID() != null && chat.getLectureID() > 0) {
                        filteredChatList.add(chat);  // 강연 ID가 있는 채팅만 추가
                    }
                }
            }

            if (filteredChatList.isEmpty()) {
                Log.e("ChatListActivity", "필터링된 채팅 목록이 없습니다.");
            }
        }

        // chatListAdapter가 초기화되지 않은 경우 초기화
        if (chatListAdapter == null) {
            chatListAdapter = new ChatListAdapter(ChatListActivity.this, filteredChatList, loggedInUserId);
            chatListView.setAdapter(chatListAdapter);
        } else {
            // 필터링된 채팅 목록을 어댑터에 설정
            chatListAdapter.updateChatList(filteredChatList);
            chatListAdapter.notifyDataSetChanged();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 모든 필터 버튼의 선택 상태를 초기화하는 메서드
    private void resetFilterButtons() {
        TextView[] filterButtons = {findViewById(R.id.button_all), findViewById(R.id.button_education), findViewById(R.id.button_lecture)};

        for (TextView button : filterButtons) {
            button.setSelected(false);
            // 기본 상태로 버튼 배경 설정 ('round_button_background.xml')
            button.setBackgroundResource(R.drawable.round_button_background);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}