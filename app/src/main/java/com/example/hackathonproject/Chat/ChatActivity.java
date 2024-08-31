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
    private int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 클래스 변수
    private int otherUserId;
    private Integer educationID;  // 교육 ID를 위한 변수 (null일 수 있음)
    private Integer lectureID;  // 강연 ID를 위한 변수 (null일 수 있음)
    private Connection connection;
    private String otherUserName; // 상대방 이름을 저장할 변수
    private Handler handler;  // Handler for periodic updates
    private Runnable refreshRunnable;
    private ChatDAO chatDAO; // ChatDAO 객체 추가
    private ImageButton chatMenuButton;



    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 세션에서 로그인된 사용자 ID를 가져옴
        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();

        // Intent로 전달된 chatId, otherUserId, educationID, lectureID 값을 가져옴
        chatId = getIntent().getIntExtra("chatId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        educationID = getIntent().hasExtra("educationId") ? getIntent().getIntExtra("educationId", -1) : null;
        lectureID = getIntent().hasExtra("lectureId") ? getIntent().getIntExtra("lectureId", -1) : null;

        // ImageButton 초기화
        chatMenuButton = findViewById(R.id.chat_menu_button);

        // ImageButton 클릭 시 PopupMenu 표시
        chatMenuButton.setOnClickListener(view -> showPopupMenu(view));

        // UI 초기화
        initializeUI();

        // ChatDAO 초기화
        initializeChatDAO();
        // Handler and Runnable 설정
        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadChatMessages(); // 주기적으로 메시지를 로드합니다.
                handler.postDelayed(this, 2000); // 0.5초마다 반복 실행
            }
        };

        handler.post(refreshRunnable); // Runnable 실행 시작

    }

    @SuppressLint("StaticFieldLeak")
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);

        // 채팅방 나가기 메뉴 추가
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
                            completeAgreement();  // 약속 완료 메서드 호출
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();
            }
        }.execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("StaticFieldLeak")
    private void leaveChatRoom() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    ensureConnectionIsOpen(); // 연결 상태 확인

                    if (chatDAO != null) {
                        return chatDAO.deleteChatRoom(chatId); // 채팅방 삭제
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

                    // 삭제 후 ChatListActivity로 돌아가기 전에 목록 새로고침을 트리거하는 코드 추가
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish(); // 현재 액티비티를 종료하여 이전 화면으로 돌아감
                } else {
                    Toast.makeText(ChatActivity.this, "채팅방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------


    @SuppressLint("StaticFieldLeak")
    private void completeAgreement() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    ensureConnectionIsOpen(); // 연결 상태 확인

                    int fee = 0;

                    // Chat 테이블에서 EducationID 또는 LectureID 값을 가져오기
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

                    // EducationID가 있는 경우 해당 Fee 값을 가져옴
                    if (educationID != null && educationID > 0) {
                        Log.d(TAG, "Fetching fee for EducationID: " + educationID);
                        String feeQuery = "SELECT Fee FROM Education WHERE EducationID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(feeQuery)) {
                            pstmt.setInt(1, educationID);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    fee = rs.getInt("Fee");
                                    Log.d(TAG, "Fee for Education: " + fee);
                                }
                            }
                        }
                    }
                    // LectureID가 있는 경우 해당 Fee 값을 가져옴
                    else if (lectureID != null && lectureID > 0) {
                        Log.d(TAG, "Fetching fee for LectureID: " + lectureID);
                        String feeQuery = "SELECT Fee FROM Lecture WHERE LectureID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(feeQuery)) {
                            pstmt.setInt(1, lectureID);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    fee = rs.getInt("Fee");
                                    Log.d(TAG, "Fee for Lecture: " + fee);
                                }
                            }
                        }
                    }

                    if (fee > 0) {
                        // Balance 업데이트
                        Log.d(TAG, "Updating Balance for OtherUserID: " + otherUserId);
                        String updateBalanceQuery = "UPDATE User SET Balance = Balance + ? WHERE UserID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(updateBalanceQuery)) {
                            pstmt.setInt(1, fee);
                            pstmt.setInt(2, otherUserId);
                            int rowsUpdated = pstmt.executeUpdate();
                            Log.d(TAG, "Balance updated, rows affected: " + rowsUpdated);
                        }

                        // Education 또는 Lecture 삭제
                        if (educationID != null && educationID > 0) {
                            Log.d(TAG, "Deleting Education record with ID: " + educationID);
                            String deleteEducationQuery = "DELETE FROM Education WHERE EducationID = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(deleteEducationQuery)) {
                                pstmt.setInt(1, educationID);
                                int rowsDeleted = pstmt.executeUpdate();
                                Log.d(TAG, "Education deleted, rows affected: " + rowsDeleted);
                            }
                        } else if (lectureID != null && lectureID > 0) {
                            Log.d(TAG, "Deleting Lecture record with ID: " + lectureID);
                            String deleteLectureQuery = "DELETE FROM Lecture WHERE LectureID = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(deleteLectureQuery)) {
                                pstmt.setInt(1, lectureID);
                                int rowsDeleted = pstmt.executeUpdate();
                                Log.d(TAG, "Lecture deleted, rows affected: " + rowsDeleted);
                            }
                        }

                        // Chat 메시지 삭제
                        Log.d(TAG, "Deleting Chat Messages for ChatID: " + chatId);
                        String deleteChatMessagesQuery = "DELETE FROM ChatMessage WHERE ChatID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(deleteChatMessagesQuery)) {
                            pstmt.setInt(1, chatId);
                            int rowsDeleted = pstmt.executeUpdate();
                            Log.d(TAG, "Chat Messages deleted, rows affected: " + rowsDeleted);
                        }

                        // Chat 삭제
                        Log.d(TAG, "Deleting Chat for ChatID: " + chatId);
                        String deleteChatQuery = "DELETE FROM Chat WHERE ChatID = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(deleteChatQuery)) {
                            pstmt.setInt(1, chatId);
                            int rowsDeleted = pstmt.executeUpdate();
                            Log.d(TAG, "Chat deleted, rows affected: " + rowsDeleted);
                        }

                        return true;
                    } else {
                        Log.e(TAG, "Fee is 0, cannot complete agreement");
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
                    finish(); // 현재 액티비티를 종료하여 이전 화면으로 돌아감
                } else {
                    Toast.makeText(ChatActivity.this, "약속 완료 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private void initializeChatDAO() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
            @Override
            public void onSuccess(Connection conn) {
                connection = conn;
                chatDAO = new ChatDAO(connection);
                // 여기서 추가 작업 수행 (예: 채팅방 생성 또는 불러오기)
                createOrRetrieveChatRoom(loggedInUserId, otherUserId);
            }

            @Override
            public void onError(SQLException e) {
                Log.e(TAG, "Database connection error: " + e.getMessage());
                showErrorMessage("데이터베이스 연결에 실패했습니다.");
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // UI 요소를 초기화하는 메서드
    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // 로그로 확인
        Log.d(TAG, "LoggedInUserId before ChatAdapter creation: " + loggedInUserId);

        // 리사이클러뷰와 어댑터 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(loggedInUserId);  // ChatAdapter에 로그인된 사용자 ID를 전달
        recyclerView.setAdapter(chatAdapter);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅방을 생성하거나 기존 채팅방을 가져오는 메서드
    private void createOrRetrieveChatRoom(int loggedInUserId, int otherUserId) {
        if (chatDAO == null) {
            // ChatDAO가 아직 초기화되지 않은 경우 초기화
            DatabaseConnection databaseConnection = new DatabaseConnection();
            databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
                @Override
                public void onSuccess(Connection conn) {
                    connection = conn;  // 데이터베이스 연결 성공 시 연결 객체를 저장
                    chatDAO = new ChatDAO(connection); // ChatDAO 초기화
                    Log.d(TAG, "Database connection established.");
                    fetchOtherUserNameAndCreateRoom(loggedInUserId, otherUserId);
                }

                @Override
                public void onError(SQLException e) {
                    runOnUiThread(() -> showErrorMessage("데이터베이스 연결에 실패했습니다."));
                    Log.e(TAG, "Database connection error: " + e.getMessage());
                }
            });
        } else {
            // 이미 ChatDAO가 초기화되어 있는 경우 바로 채팅방 생성/조회
            fetchOtherUserNameAndCreateRoom(loggedInUserId, otherUserId);
        }
    }

    private void fetchOtherUserNameAndCreateRoom(int loggedInUserId, int otherUserId) {
        // 상대방의 이름을 가져오는 메서드 호출
        fetchOtherUserName(otherUserId, new UserNameCallback() {
            @Override
            public void onUserNameRetrieved(String userName) {
                otherUserName = userName; // 상대방 이름 저장
                if (chatId == -1) {
                    // 채팅방이 존재하지 않으면 새로 생성
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, educationID, lectureID, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int retrievedChatId) {
                            runOnUiThread(() -> {
                                chatId = retrievedChatId;  // 가져온 채팅 ID를 저장
                                initializeChatUI(loggedInUserId, otherUserName);
                                loadChatMessages(); // 채팅 메시지를 로드합니다.
                                markMessagesAsRead(chatId); // 메시지를 읽음으로 표시
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
                    initializeChatUI(loggedInUserId, otherUserName);
                    loadChatMessages();
                    markMessagesAsRead(chatId); // 메시지를 읽음으로 표시
                }
            }

            @Override
            public void onError(SQLException e) {
                runOnUiThread(() -> showErrorMessage("상대방 이름을 불러오는 데 실패했습니다."));
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비동기로 상대방의 이름을 가져오는 메서드
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
                        userName = resultSet.getString("Name");  // 사용자 이름을 쿼리 결과에서 가져옴
                    }
                } catch (SQLException e) {
                    exception = e;  // 예외 발생 시 저장
                }
                return userName;
            }

            @Override
            protected void onPostExecute(String userName) {
                if (exception != null) {
                    callback.onError(exception);  // 예외가 발생했으면 콜백의 onError 호출
                } else {
                    callback.onUserNameRetrieved(userName);  // 사용자 이름을 콜백에 전달
                }
            }
        }.execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅 UI를 초기화하는 메서드
    private void initializeChatUI(int loggedInUserId, String otherUserName) {
        sendButton.setOnClickListener(v -> sendMessage());  // 전송 버튼 클릭 시 메시지 전송 메서드 호출
        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText(otherUserName + "님");  // 채팅창 타이틀에 상대방 이름 표시
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅 메시지를 로드하는 메서드
    private void loadChatMessages() {
        if (connection != null) {
            Log.d(TAG, "Database connection is established, loading messages...");
            new LoadMessagesTask().execute(chatId);
        } else {
            Log.e(TAG, "Database connection is null, unable to load messages.");
//            showErrorMessage("데이터베이스 연결이 설정되지 않았습니다.");
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비동기로 채팅 메시지를 로드하는 AsyncTask 클래스
    private class LoadMessagesTask extends AsyncTask<Integer, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(Integer... params) {
            int chatId = params[0];

            try (Connection conn = new DatabaseConnection().connect()) { // 새 연결 열기
                if (conn != null) {
                    ChatMessageDAO chatMessageDAO = new ChatMessageDAO(conn);
                    return chatMessageDAO.getMessagesByChatId(chatId, loggedInUserId);  // 채팅 ID로 메시지 가져오기
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
                chatAdapter.setMessages(messages);  // 가져온 메시지를 어댑터에 설정
                recyclerView.scrollToPosition(messages.size() - 1); // 스크롤을 최신 메시지로 이동
            } else {
                showErrorMessage("메시지를 불러오는데 실패했습니다.");
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 메시지를 전송하는 메서드
    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // 현재 시간을 KST(한국 표준시)로 가져옴
            ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            new SendMessageTask().execute(messageText, kstTime.toString());
        } else {
            Toast.makeText(ChatActivity.this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 메시지를 전송하는 메서드
    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String messageText = params[0];
            String kstTimeString = params[1];

            ZonedDateTime kstTime = ZonedDateTime.parse(kstTimeString); // 문자열을 ZonedDateTime으로 변환

            try {
                // 연결이 유효하지 않으면 새로 연결 시도
                ensureConnectionIsOpen();

                if (chatId > 0) {
                    // 메시지를 추가하는 작업
                    ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                    boolean messageAdded = chatMessageDAO.addMessage(chatId, loggedInUserId, messageText, kstTime);

                    if (messageAdded) {
                        // 읽음 상태 업데이트
                        updateReadStatus();
                    }
                    return messageAdded;
                } else {
                    Log.e(TAG, "Invalid ChatID: " + chatId);
                    return false;
                }
            } catch (SQLException e) {
                Log.e(TAG, "Failed to reconnect to database", e);
                return false; // 연결 실패 시 작업 중단
            }
        }

        private void updateReadStatus() {
            try {
                // 연결이 유효하지 않으면 새로 연결 시도
                ensureConnectionIsOpen();

                String updateQuery = "UPDATE Chat SET " +
                        "IsAuthorMessageRead = CASE WHEN AuthorID = ? THEN TRUE ELSE FALSE END, " +
                        "IsOtherUserMessageRead = CASE WHEN OtherUserID = ? THEN TRUE ELSE FALSE END " +
                        "WHERE ChatID = ?";

                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setInt(1, loggedInUserId);  // 현재 사용자가 AuthorID이면 IsAuthorMessageRead를 TRUE로 설정
                    statement.setInt(2, loggedInUserId);  // 현재 사용자가 OtherUserID이면 IsOtherUserMessageRead를 TRUE로 설정
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
                inputMessage.setText("");  // 메시지 입력창 초기화
                loadChatMessages();  // 메시지 전송 후 채팅 메시지 다시 로드
            } else {
                showErrorMessage("메시지 전송에 실패했습니다.");
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 메시지 읽음 상태를 업데이트하는 메서드
    private void markMessagesAsRead(int chatId) {
        new Thread(() -> {
            try {
                // 연결이 유효하지 않으면 새로 연결 시도
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 연결이 유효한지 확인하고 필요하면 새로 연결하는 메서드
    private void ensureConnectionIsOpen() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = new DatabaseConnection().connect(); // 새 연결 시도
            chatDAO = new ChatDAO(connection); // 새로운 연결로 ChatDAO 초기화
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        // 채팅방에 들어올 때 읽음 상태 업데이트
        markMessagesAsRead(chatId);

        // 화면이 보일 때만 새로고침을 시작
        handler.post(refreshRunnable);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();

        // 화면이 보이지 않으면 새로고침을 중지
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 에러 메시지를 화면에 표시하는 메서드
    private void showErrorMessage(String message) {
        runOnUiThread(() -> Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show());
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 액티비티가 종료될 때 데이터베이스 연결을 닫는 메서드
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

        // Handler의 반복 실행을 중지하기 전에 null 확인
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 이름을 비동기적으로 가져오기 위한 콜백 인터페이스 정의
    interface UserNameCallback {
        void onUserNameRetrieved(String userName);  // 사용자 이름을 성공적으로 가져왔을 때 호출

        void onError(SQLException e);  // 오류가 발생했을 때 호출
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

}