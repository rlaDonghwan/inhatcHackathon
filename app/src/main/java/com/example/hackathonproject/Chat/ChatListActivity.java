package com.example.hackathonproject.Chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
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
    private ListView chatListView;
    private List<Chat> chatList;
    private List<Chat> filteredChatList;
    private ChatDAO chatDAO;
    private ChatListAdapter chatListAdapter;
    private int loggedInUserId;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView filterAllButton;
    private TextView filterEducationButton;
    private TextView filterLectureButton;

    private Runnable refreshRunnable;
    private Handler handler;
    private Connection connection;

    private static final String CHANNEL_ID = "chat_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chat_list_view);

        filterAllButton = findViewById(R.id.button_all);
        filterAllButton.setSelected(true);
        filterEducationButton = findViewById(R.id.button_education);
        filterLectureButton = findViewById(R.id.button_lecture);

        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 20);

        filterAllButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        filterEducationButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        filterLectureButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new LoadChatListTask(DatabaseConnection.getInstance()).execute();
        });

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        new LoadChatListTask(DatabaseConnection.getInstance()).execute();

        filterAllButton = findViewById(R.id.button_all);
        filterAllButton.setSelected(true);
        filterEducationButton = findViewById(R.id.button_education);
        filterLectureButton = findViewById(R.id.button_lecture);

        filterAllButton.setOnClickListener(view -> setFilter(filterAllButton, "전체"));
        filterEducationButton.setOnClickListener(view -> setFilter(filterEducationButton, "교육"));
        filterLectureButton.setOnClickListener(view -> setFilter(filterLectureButton, "강연"));

        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
        });

        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            Chat selectedChat = filteredChatList.get(position);

            if (selectedChat.getAuthorID() == loggedInUserId) {
                selectedChat.setOtherUserMessageRead(true);
            } else {
                selectedChat.setAuthorMessageRead(true);
            }

            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", selectedChat.getChatID());
            intent.putExtra("otherUserId", selectedChat.getCounterpartUserID(loggedInUserId));
            intent.putExtra("educationID", selectedChat.getEducationID());
            intent.putExtra("lectureID", selectedChat.getLectureID());
            startActivityForResult(intent, 1);
        });

        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                new LoadChatListTask(DatabaseConnection.getInstance()).execute();
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChatList();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            refreshChatList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    private void refreshChatList() {
        swipeRefreshLayout.setRefreshing(true);
        new LoadChatListTask(DatabaseConnection.getInstance()).execute();
    }

    private class LoadChatListTask extends AsyncTask<Void, Void, List<Chat>> {
        private DatabaseConnection databaseConnection;
        private Connection connection;

        public LoadChatListTask(DatabaseConnection databaseConnection) {
            this.databaseConnection = databaseConnection;
        }

        @Override
        protected List<Chat> doInBackground(Void... voids) {
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
            swipeRefreshLayout.setRefreshing(false);
            if (chats != null && !chats.isEmpty()) {
                chatList = chats;
                filteredChatList = new ArrayList<>(chatList);
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, filteredChatList, loggedInUserId);
                chatListView.setAdapter(chatListAdapter);
            } else {
                if (chats == null) {
                    Log.e("LoadChatListTask", "Chats list is null.");
                } else {
                    Log.e("LoadChatListTask", "Chats list is empty.");
                }
            }
        }
    }

    private void setFilter(TextView selectedButton, String filterType) {
        resetFilterButtons();
        selectedButton.setSelected(true);
        selectedButton.setBackgroundResource(R.drawable.round_button_background_selected);

        if (chatList == null || chatList.isEmpty()) {
            filteredChatList = new ArrayList<>();
        } else {
            if (filterType.equals("전체")) {
                filteredChatList = new ArrayList<>(chatList);
            } else if (filterType.equals("교육")) {
                filteredChatList = new ArrayList<>();
                for (Chat chat : chatList) {
                    if (chat.getEducationID() != null && chat.getEducationID() > 0) {
                        filteredChatList.add(chat);
                    }
                }
            } else if (filterType.equals("강연")) {
                filteredChatList = new ArrayList<>();
                for (Chat chat : chatList) {
                    if (chat.getLectureID() != null && chat.getLectureID() > 0) {
                        filteredChatList.add(chat);
                    }
                }
            }
        }

        if (chatListAdapter == null) {
            chatListAdapter = new ChatListAdapter(ChatListActivity.this, filteredChatList, loggedInUserId);
            chatListView.setAdapter(chatListAdapter);
        } else {
            chatListAdapter.updateChatList(filteredChatList);
            chatListAdapter.notifyDataSetChanged();
        }
    }

    private void resetFilterButtons() {
        TextView[] filterButtons = {findViewById(R.id.button_all), findViewById(R.id.button_education), findViewById(R.id.button_lecture)};

        for (TextView button : filterButtons) {
            button.setSelected(false);
            button.setBackgroundResource(R.drawable.round_button_background);
        }
    }
}