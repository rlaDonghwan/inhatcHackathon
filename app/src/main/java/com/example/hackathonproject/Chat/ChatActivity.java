package com.example.hackathonproject.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import java.sql.PreparedStatement;
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
    private Integer educationID;
    private Integer lectureID;
    private Connection connection;
    private String otherUserName;
    private Handler handler;
    private Runnable refreshRunnable;
    private ChatDAO chatDAO;
    private ImageButton chatMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        chatId = getIntent().getIntExtra("chatId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        educationID = getIntent().hasExtra("educationId") ? getIntent().getIntExtra("educationId", -1) : null;
        lectureID = getIntent().hasExtra("lectureId") ? getIntent().getIntExtra("lectureId", -1) : null;

        chatMenuButton = findViewById(R.id.chat_menu_button);
        chatMenuButton.setOnClickListener(view -> showPopupMenu(view));

        initializeUI();

        initializeChatDAO();

        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadChatMessages();
                handler.postDelayed(this, 2000);
            }
        };

        handler.post(refreshRunnable);
    }

    @SuppressLint("StaticFieldLeak")
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 0, 0, "채팅방 나가기");

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Chat chat = chatDAO.getChatById(chatId);
                    return loggedInUserId == chat.getAuthorID();
                } catch (SQLException e) {
                    Log.e(TAG, "Failed to fetch chat details", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean isAuthor) {
                if (isAuthor) {
                    popup.getMenu().add(0, 1, 1, "약속 완료");
                }
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0:
                            leaveChatRoom();
                            return true;
                        case 1:
                            completeAgreement();
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void leaveChatRoom() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    ensureConnectionIsOpen();

                    if (chatDAO != null) {
                        return chatDAO.deleteChatRoom(chatId);
                    } else {
                        return false;
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "Failed to delete chat room", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(ChatActivity.this, "채팅방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(ChatActivity.this, "채팅방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void completeAgreement() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    ensureConnectionIsOpen();

                    int fee = 0;

                    String query = "SELECT EducationID, LectureID FROM Chat WHERE ChatID = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                        pstmt.setInt(1, chatId);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                educationID = rs.getInt("EducationID");
                                lectureID = rs.getInt("LectureID");
                            }
                        }
                    }

                    if (educationID != null && educationID > 0) {
                        String feeQuery = "SELECT Fee FROM Education WHERE EducationID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(feeQuery)) {
                            pstmt.setInt(1, educationID);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    fee = rs.getInt("Fee");
                                }
                            }
                        }
                    } else if (lectureID != null && lectureID > 0) {
                        String feeQuery = "SELECT Fee FROM Lecture WHERE LectureID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(feeQuery)) {
                            pstmt.setInt(1, lectureID);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    fee = rs.getInt("Fee");
                                }
                            }
                        }
                    }

                    if (fee > 0) {
                        String updateBalanceQuery = "UPDATE User SET Balance = Balance + ? WHERE UserID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(updateBalanceQuery)) {
                            pstmt.setInt(1, fee);
                            pstmt.setInt(2, otherUserId);
                            pstmt.executeUpdate();
                        }

                        if (educationID != null && educationID > 0) {
                            String deleteEducationQuery = "DELETE FROM Education WHERE EducationID = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(deleteEducationQuery)) {
                                pstmt.setInt(1, educationID);
                                pstmt.executeUpdate();
                            }
                        } else if (lectureID != null && lectureID > 0) {
                            String deleteLectureQuery = "DELETE FROM Lecture WHERE LectureID = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(deleteLectureQuery)) {
                                pstmt.setInt(1, lectureID);
                                pstmt.executeUpdate();
                            }
                        }

                        String deleteChatMessagesQuery = "DELETE FROM ChatMessage WHERE ChatID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(deleteChatMessagesQuery)) {
                            pstmt.setInt(1, chatId);
                            pstmt.executeUpdate();
                        }

                        String deleteChatQuery = "DELETE FROM Chat WHERE ChatID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(deleteChatQuery)) {
                            pstmt.setInt(1, chatId);
                            pstmt.executeUpdate();
                        }

                        return true;
                    } else {
                        return false;
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "약속 완료 처리 중 오류 발생", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(ChatActivity.this, "약속이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(ChatActivity.this, "약속 완료 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void initializeChatDAO() {
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance(); // 수정된 부분
        databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
            @Override
            public void onSuccess(Connection conn) {
                connection = conn;
                chatDAO = new ChatDAO(connection);
                createOrRetrieveChatRoom(loggedInUserId, otherUserId);
            }

            @Override
            public void onError(SQLException e) {
                Log.e(TAG, "Database connection error: " + e.getMessage());
                showErrorMessage("데이터베이스 연결에 실패했습니다.");
            }
        });
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

    private void createOrRetrieveChatRoom(int loggedInUserId, int otherUserId) {
        if (chatDAO == null) {
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance(); // 수정된 부분
            databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
                @Override
                public void onSuccess(Connection conn) {
                    connection = conn;
                    chatDAO = new ChatDAO(connection);
                    fetchOtherUserNameAndCreateRoom(loggedInUserId, otherUserId);
                }

                @Override
                public void onError(SQLException e) {
                    runOnUiThread(() -> showErrorMessage("데이터베이스 연결에 실패했습니다."));
                    Log.e(TAG, "Database connection error: " + e.getMessage());
                }
            });
        } else {
            fetchOtherUserNameAndCreateRoom(loggedInUserId, otherUserId);
        }
    }

    private void fetchOtherUserNameAndCreateRoom(int loggedInUserId, int otherUserId) {
        fetchOtherUserName(otherUserId, new UserNameCallback() {
            @Override
            public void onUserNameRetrieved(String userName) {
                otherUserName = userName;
                if (chatId == -1) {
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, educationID, lectureID, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int retrievedChatId) {
                            runOnUiThread(() -> {
                                chatId = retrievedChatId;
                                initializeChatUI(loggedInUserId, otherUserName);
                                loadChatMessages();
                                markMessagesAsRead(chatId);
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> showErrorMessage("채팅방을 생성할 수 없습니다. 다시 시도해 주세요."));
                            Log.e(TAG, "Error creating chat room: " + e.getMessage());
                        }
                    });
                } else {
                    initializeChatUI(loggedInUserId, otherUserName);
                    loadChatMessages();
                    markMessagesAsRead(chatId);
                }
            }

            @Override
            public void onError(SQLException e) {
                runOnUiThread(() -> showErrorMessage("상대방 이름을 불러오는 데 실패했습니다."));
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
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

    private void initializeChatUI(int loggedInUserId, String otherUserName) {
        sendButton.setOnClickListener(v -> sendMessage());
        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText(otherUserName + "님");
    }

    private void loadChatMessages() {
        if (connection != null) {
            new LoadMessagesTask().execute(chatId);
        } else {
            Log.e(TAG, "Database connection is null, unable to load messages.");
        }
    }

    private class LoadMessagesTask extends AsyncTask<Integer, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(Integer... params) {
            int chatId = params[0];

            try (Connection conn = DatabaseConnection.getInstance().connect()) { // 수정된 부분
                if (conn != null) {
                    ChatMessageDAO chatMessageDAO = new ChatMessageDAO(conn);
                    return chatMessageDAO.getMessagesByChatId(chatId, loggedInUserId);
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
                recyclerView.scrollToPosition(messages.size() - 1);
            } else {
                showErrorMessage("메시지를 불러오는데 실패했습니다.");
            }
        }
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
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

            ZonedDateTime kstTime = ZonedDateTime.parse(kstTimeString);

            try {
                ensureConnectionIsOpen();

                if (chatId > 0) {
                    ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                    boolean messageAdded = chatMessageDAO.addMessage(chatId, loggedInUserId, messageText, kstTime);

                    if (messageAdded) {
                        updateReadStatus();
                    }
                    return messageAdded;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                Log.e(TAG, "Failed to reconnect to database", e);
                return false;
            }
        }

        private void updateReadStatus() {
            try {
                ensureConnectionIsOpen();

                String updateQuery = "UPDATE Chat SET " +
                        "IsAuthorMessageRead = CASE WHEN AuthorID = ? THEN TRUE ELSE FALSE END, " +
                        "IsOtherUserMessageRead = CASE WHEN OtherUserID = ? THEN TRUE ELSE FALSE END " +
                        "WHERE ChatID = ?";

                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setInt(1, loggedInUserId);
                    statement.setInt(2, loggedInUserId);
                    statement.setInt(3, chatId);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Log.e(TAG, "Failed to update read status", e);
            }
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

    private void markMessagesAsRead(int chatId) {
        new Thread(() -> {
            try {
                ensureConnectionIsOpen();

                String query;
                Chat chat = chatDAO.getChatById(chatId);

                if (chat != null) {
                    if (chat.getAuthorID() == loggedInUserId) {
                        query = "UPDATE Chat SET IsOtherUserMessageRead = TRUE WHERE ChatID = ?";
                    } else {
                        query = "UPDATE Chat SET IsAuthorMessageRead = TRUE WHERE ChatID = ?";
                    }

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, chatId);
                        statement.executeUpdate();
                    }
                } else {
                    Log.e(TAG, "Chat not found for chatId: " + chatId);
                }
            } catch (SQLException e) {
                Log.e(TAG, "Failed to update message read status", e);
            }
        }).start();
    }

    private void ensureConnectionIsOpen() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getInstance().connect(); // 수정된 부분
            chatDAO = new ChatDAO(connection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        markMessagesAsRead(chatId);
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
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

        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    interface UserNameCallback {
        void onUserNameRetrieved(String userName);
        void onError(SQLException e);
    }
}