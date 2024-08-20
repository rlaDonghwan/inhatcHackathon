package com.example.hackathonproject.Chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.ChatMessageDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.Login.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputMessage;
    private ImageButton sendButton;
    private int chatId;
    private int loggedInUserId;
    private int otherUserId;
    private int postId;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeUI();

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        chatId = getIntent().getIntExtra("chatId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        postId = getIntent().getIntExtra("postId", -1);
        int currentUserId = getIntent().getIntExtra("currentUserId", -1);

        // 데이터베이스 연결을 시도하고 완료된 후 UI 초기화 및 데이터 로드
        createOrRetrieveChatRoom(currentUserId, otherUserId);
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(loggedInUserId);
        recyclerView.setAdapter(chatAdapter);
    }

    private void createOrRetrieveChatRoom(int currentUserId, int otherUserId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
            @Override
            public void onSuccess(Connection conn) {
                connection = conn;
                Log.d(TAG, "Database connection established.");
                if (chatId == -1) {
                    ChatDAO chatDAO = new ChatDAO(connection);
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, postId, null, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int retrievedChatId) {
                            runOnUiThread(() -> {
                                chatId = retrievedChatId;
                                initializeChatUI(currentUserId, otherUserId);
                                loadChatMessages(); // 채팅 메시지를 로드합니다.
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> showErrorMessage("채팅방을 생성할 수 없습니다. 다시 시도해 주세요."));
                            Log.e(TAG, "Error creating chat room: " + e.getMessage());
                        }
                    });
                } else {
                    // 기존 채팅방이 있을 때
                    initializeChatUI(currentUserId, otherUserId);
                    loadChatMessages();
                }
            }

            @Override
            public void onError(SQLException e) {
                runOnUiThread(() -> showErrorMessage("데이터베이스 연결에 실패했습니다."));
                Log.e(TAG, "Database connection error: " + e.getMessage());
            }
        });
    }

    private void initializeChatUI(int currentUserId, int otherUserId) {
        sendButton.setOnClickListener(v -> sendMessage());
        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText("Chat between User " + currentUserId + " and User " + otherUserId);
    }

    private void loadChatMessages() {
        if (connection != null) {
            new LoadMessagesTask().execute(chatId);
        } else {
            showErrorMessage("데이터베이스 연결이 설정되지 않았습니다.");
        }
    }

    private class LoadMessagesTask extends AsyncTask<Integer, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(Integer... params) {
            int chatId = params[0];
            if (connection != null) {
                ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                return chatMessageDAO.getMessagesByChatId(chatId);
            } else {
                Log.e(TAG, "Connection is null in LoadMessagesTask.");
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ChatMessage> messages) {
            if (messages != null) {
                chatAdapter.setMessages(messages);
            } else {
                showErrorMessage("메시지를 불러오는데 실패했습니다.");
            }
        }
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            new SendMessageTask().execute(messageText);
        } else {
            Toast.makeText(ChatActivity.this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String messageText = params[0];
            if (connection != null) {
                ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                if (chatId > 0) {
                    chatMessageDAO.addMessage(chatId, loggedInUserId, messageText);
                    return true;
                } else {
                    Log.e(TAG, "Invalid ChatID: " + chatId);
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                inputMessage.setText("");
                loadChatMessages();
            } else {
                showErrorMessage("메시지 전송에 실패했습니다.");
            }
        }
    }

    private void showErrorMessage(String message) {
        runOnUiThread(() -> Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing database connection: " + e.getMessage());
            }
        }
    }
}
