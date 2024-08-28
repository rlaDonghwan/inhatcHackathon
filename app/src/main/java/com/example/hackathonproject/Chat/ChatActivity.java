package com.example.hackathonproject.Chat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private int loggedInUserId;  // 로그인된 사용자 ID를 저장하는 클래스 변수
    private int otherUserId;
    private int postId;
    private Connection connection;
    private String otherUserName; // 상대방 이름을 저장할 변수
    private Handler handler;  // Handler for periodic updates
    private Runnable refreshRunnable;

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 먼저 SessionManager를 통해 loggedInUserId를 가져옴
        SessionManager sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserId();  // 세션에서 로그인된 사용자 ID를 가져와서 클래스 변수에 설정

        Log.d("ChatActivity", "LoggedInUserId in onCreate: " + loggedInUserId);  // 로그로 확인

        // Intent로 전달된 chatId, otherUserId, postId 값을 가져옴
        chatId = getIntent().getIntExtra("chatId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        postId = getIntent().getIntExtra("postId", -1);
        int currentUserId = getIntent().getIntExtra("currentUserId", -1);

        // UI를 초기화
        initializeUI();

        // 데이터베이스 연결을 시도하고 완료된 후 UI 초기화 및 데이터 로드
        createOrRetrieveChatRoom(currentUserId, otherUserId);

        // Handler and Runnable 설정
        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadChatMessages(); // 주기적으로 메시지를 로드합니다.
                handler.postDelayed(this, 1000); // 1초마다 반복 실행
            }
        };
        handler.post(refreshRunnable); // Runnable 실행 시작
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
        Log.d("ChatActivity", "LoggedInUserId before ChatAdapter creation: " + loggedInUserId);

        // 리사이클러뷰와 어댑터 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(loggedInUserId);  // ChatAdapter에 로그인된 사용자 ID를 전달
        recyclerView.setAdapter(chatAdapter);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅방을 생성하거나 기존 채팅방을 가져오는 메서드
    private void createOrRetrieveChatRoom(int currentUserId, int otherUserId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
            @Override
            public void onSuccess(Connection conn) {
                connection = conn;  // 데이터베이스 연결 성공 시 연결 객체를 저장
                Log.d(TAG, "Database connection established.");

                // 상대방의 이름을 가져오는 메서드 호출
                fetchOtherUserName(otherUserId, new UserNameCallback() {
                    @Override
                    public void onUserNameRetrieved(String userName) {
                        otherUserName = userName; // 상대방 이름 저장
                        if (chatId == -1) {
                            // 채팅방이 존재하지 않으면 새로 생성
                            ChatDAO chatDAO = new ChatDAO(connection);
                            chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, postId, null, new ChatDAO.ChatRoomCallback() {
                                @Override
                                public void onSuccess(int retrievedChatId) {
                                    runOnUiThread(() -> {
                                        chatId = retrievedChatId;  // 가져온 채팅 ID를 저장
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
    private void initializeChatUI(int currentUserId, String otherUserName) {
        sendButton.setOnClickListener(v -> sendMessage());  // 전송 버튼 클릭 시 메시지 전송 메서드 호출
        TextView chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText(otherUserName + "님");  // 채팅창 타이틀에 상대방 이름 표시
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅 메시지를 로드하는 메서드
    private void loadChatMessages() {
        if (connection != null) {
            Log.d("ChatActivity", "Database connection is established, loading messages...");
            new LoadMessagesTask().execute(chatId);
        } else {
            Log.e("ChatActivity", "Database connection is null, unable to load messages.");
            showErrorMessage("데이터베이스 연결이 설정되지 않았습니다.");
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

    // 비동기로 메시지를 전송하는 AsyncTask 클래스
    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String messageText = params[0];
            String kstTimeString = params[1];

            ZonedDateTime kstTime = ZonedDateTime.parse(kstTimeString); // 문자열을 ZonedDateTime으로 변환

            if (connection != null) {
                ChatMessageDAO chatMessageDAO = new ChatMessageDAO(connection);
                if (chatId > 0) {
                    return chatMessageDAO.addMessage(chatId, loggedInUserId, messageText, kstTime);  // 메시지를 데이터베이스에 저장
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
                inputMessage.setText("");  // 메시지 입력창 초기화
                loadChatMessages();  // 메시지 전송 후 채팅 메시지 다시 로드
            } else {
                showErrorMessage("메시지 전송에 실패했습니다.");
            }
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

        // Handler의 반복 실행을 중지
        handler.removeCallbacks(refreshRunnable);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 이름을 비동기적으로 가져오기 위한 콜백 인터페이스 정의
    interface UserNameCallback {
        void onUserNameRetrieved(String userName);  // 사용자 이름을 성공적으로 가져왔을 때 호출
        void onError(SQLException e);  // 오류가 발생했을 때 호출
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

}
