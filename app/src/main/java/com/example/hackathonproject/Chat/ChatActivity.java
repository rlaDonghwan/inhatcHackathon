package com.example.hackathonproject.Chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputMessage;
    private ImageButton sendButton;
    private int chatId;
    private int loggedInUserId;
    private int otherUserId; // 추가: 상대방 사용자 ID
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // back_button 클릭 시 뒤로 가기
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        chatId = getIntent().getIntExtra("chatId", -1); // 추가: 전달된 chatId 사용
        otherUserId = getIntent().getIntExtra("otherUserId", -1); // 추가: 전달된 otherUserId 사용
        DatabaseConnection databaseConnection = new DatabaseConnection();

        // 비동기적으로 데이터베이스 연결
        new ConnectDatabaseTask(databaseConnection).execute();
    }

    private class ConnectDatabaseTask extends AsyncTask<Void, Void, Connection> {
        private DatabaseConnection databaseConnection;

        public ConnectDatabaseTask(DatabaseConnection databaseConnection) {
            this.databaseConnection = databaseConnection;
        }

        @Override
        protected Connection doInBackground(Void... voids) {
            try {
                return databaseConnection.connect();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Connection conn) {
            if (conn != null) {
                connection = conn;
                initializeChatUI();  // UI 초기화
            } else {
                Toast.makeText(ChatActivity.this, "데이터베이스 연결에 실패했습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeChatUI() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(loggedInUserId);
        recyclerView.setAdapter(chatAdapter);

        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        loadChatMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadChatMessages() {
        new LoadMessagesTask().execute(chatId);
    }

    private class LoadMessagesTask extends AsyncTask<Integer, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(Integer... params) {
            int chatId = params[0];
            if (connection != null) {
                ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                return chatMessageDAO.getMessagesByChatId(chatId);  // 백그라운드에서 메시지 로드
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ChatMessage> messages) {
            if (messages != null) {
                chatAdapter.setMessages(messages);  // UI 업데이트는 메인 스레드에서 실행
            } else {
                Toast.makeText(ChatActivity.this, "메시지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
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
                chatMessageDAO.addMessage(chatId, loggedInUserId, messageText);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                inputMessage.setText("");  // 메시지 입력 필드 초기화
                loadChatMessages();  // 채팅 메시지 갱신
            } else {
                Toast.makeText(ChatActivity.this, "메시지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
