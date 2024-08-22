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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private String otherUserName; // 상대방 이름을 저장할 변수

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
                fetchOtherUserName(otherUserId, new UserNameCallback() {
                    @Override
                    public void onUserNameRetrieved(String userName) {
                        otherUserName = userName; // 상대방 이름 저장
                        if (chatId == -1) {
                            ChatDAO chatDAO = new ChatDAO(connection);
                            chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, postId, null, new ChatDAO.ChatRoomCallback() {
                                @Override
                                public void onSuccess(int retrievedChatId) {
                                    runOnUiThread(() -> {
                                        chatId = retrievedChatId;
                                        initializeChatUI(currentUserId, otherUserName);
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
                            initializeChatUI(currentUserId, otherUserName);
                            loadChatMessages();
                        }
                    }

                    @Override
                    public void onError(SQLException e) {
                        runOnUiThread(() -> showErrorMessage("상대방 이름을 불러오는 데 실패했습니다."));
                    }
                });
            }

            @Override
            public void onError(SQLException e) {
                runOnUiThread(() -> showErrorMessage("데이터베이스 연결에 실패했습니다."));
                Log.e(TAG, "Database connection error: " + e.getMessage());
            }
        });
    }

    private void fetchOtherUserName(int userId, UserNameCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private SQLException exception;

            @Override
            protected String doInBackground(Void... voids) {
                String userName = null;
                try {
                    Statement statement = connection.createStatement();
                    String query = "SELECT Name FROM User WHERE UserID = " + userId;
                    ResultSet resultSet = statement.executeQuery(query);
                    if (resultSet.next()) {
                        userName = resultSet.getString("Name");
                    }
                } catch (SQLException e) {
                    exception = e;
                }
                return userName;
            }

            @Override
            protected void onPostExecute(String userName) {
                if (exception != null) {
                    callback.onError(exception);
                } else {
                    callback.onUserNameRetrieved(userName);
                }
            }
        }.execute();
    }

    private void initializeChatUI(int currentUserId, String otherUserName) {
        sendButton.setOnClickListener(v -> sendMessage());
        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText(otherUserName + "님");
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

            try (Connection conn = new DatabaseConnection().connect()) { // 새 연결 열기
                if (conn != null) {
                    ChatMessageDAO chatMessageDAO = new ChatMessageDAO(conn);
                    return chatMessageDAO.getMessagesByChatId(chatId);
                } else {
                    Log.e(TAG, "Failed to establish a database connection in LoadMessagesTask.");
                    return null;
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error while loading messages: " + e.getMessage());
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
            // 현재 시간을 KST로 가져옴
            ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            new SendMessageTask().execute(messageText, kstTime.toString());
        } else {
            Toast.makeText(ChatActivity.this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String messageText = params[0];
            String kstTimeString = params[1];

            ZonedDateTime kstTime = ZonedDateTime.parse(kstTimeString); // 문자열을 ZonedDateTime으로 변환

            if (connection != null) {
                ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                if (chatId > 0) {
                    return chatMessageDAO.addMessage(chatId, loggedInUserId, messageText, kstTime);
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

    // 콜백 인터페이스 정의
    interface UserNameCallback {
        void onUserNameRetrieved(String userName);
        void onError(SQLException e);
    }
}
