package com.example.hackathonproject.Education;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.EducationDAO;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EducationWriteActivity extends AppCompatActivity {
    private EditText titleEditText;  // 제목을 입력하는 EditText
    private EditText descriptionEditText;  // 내용을 입력하는 EditText
    private EditText priceEditText;  // 금액을 입력하는 EditText
    private Button submitButton;  // 제출 버튼
    private CheckBox checkBoxBuy;  // '구해요' 카테고리 선택 CheckBox
    private CheckBox checkBoxSell;  // '할게요' 카테고리 선택 CheckBox
    private EducationDAO educationDAO;  // 데이터베이스 접근 객체
    private SessionManager sessionManager;  // 사용자 세션 관리 객체
    private int educationId = -1; // 수정 시 사용할 교육 게시글 ID

    private boolean isBuySelected = false;
    private boolean isSellSelected = false;


    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_write);

        educationDAO = new EducationDAO(); // DAO 객체 초기화
        sessionManager = new SessionManager(this); // SessionManager 객체 초기화

        ImageButton backButton = findViewById(R.id.back_button);  // 뒤로 가기 버튼 초기화
        backButton.setOnClickListener(v -> onBackPressed());  // 뒤로 가기 버튼 클릭 시 뒤로 이동

        titleEditText = findViewById(R.id.title_edit_text);  // 제목 입력 필드 초기화
        descriptionEditText = findViewById(R.id.content_edit_text);  // 내용 입력 필드 초기화
        submitButton = findViewById(R.id.btnSummit);  // 제출 버튼 초기화
        checkBoxBuy = findViewById(R.id.checkbox_buy);  // '구해요' 체크박스 초기화
        checkBoxSell = findViewById(R.id.checkbox_sell);  // '할게요' 체크박스 초기화
        priceEditText = findViewById(R.id.price_edit_text);  // 교육료 입력 필드 초기화
        TextView toolbarTitle = findViewById(R.id.toolbar_title);  // 툴바 제목 TextView 초기화


        // Intent로 전달된 데이터 처리 (수정 모드인지 확인)
        Intent intent = getIntent();
        if (intent.hasExtra("educationId")) {
            educationId = intent.getIntExtra("educationId", -1);  // 교육 게시글 ID 가져오기
            String title = intent.getStringExtra("title");  // 제목 가져오기
            String content = intent.getStringExtra("content");  // 내용 가져오기
            String category = intent.getStringExtra("category");  // 카테고리 가져오기
            int fee = intent.getIntExtra("fee", 0);  // 교육료 가져오기

            titleEditText.setText(title);  // 제목 설정
            descriptionEditText.setText(content);  // 내용 설정
            priceEditText.setText(String.valueOf(fee));  // 교육료 설정


            // 수정 모드이므로 툴바 제목을 "교육 수정"으로 변경
            toolbarTitle.setText("교육 수정");

            if ("구해요".equals(category)) {
                checkBoxBuy.setChecked(true);  // '구해요' 카테고리 선택
            } else if ("할게요".equals(category)) {
                checkBoxSell.setChecked(true);  // '할게요' 카테고리 선택
            }

            // 수정 모드로 동작
            submitButton.setOnClickListener(v -> updateEducationPost());  // 수정 버튼 클릭 시 처리
        } else {
            // 새 글 작성 모드로 동작
            submitButton.setOnClickListener(v -> submitEducation());  // 제출 버튼 클릭 시 처리
            toolbarTitle.setText("교육 신청");
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 새 교육 게시글을 등록하는 메서드
    private void submitEducation() {
        String title = titleEditText.getText().toString().trim();  // 제목 가져오기
        String description = descriptionEditText.getText().toString().trim();  // 내용 가져오기
        String category = checkBoxBuy.isChecked() ? "구해요" : checkBoxSell.isChecked() ? "할게요" : "";  // 선택된 카테고리 가져오기
        String feeStr = priceEditText.getText().toString().trim(); // 금액 가져오기
        int fee = feeStr.isEmpty() ? 0 : Integer.parseInt(feeStr); // 금액이 비어 있으면 0으로 설정

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "제목, 내용, 카테고리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();  // 필수 항목 누락 시 경고
            return;
        }

        // 비동기 작업으로 게시글 등록
        new SubmitEducationTask().execute(title, category, description, "서울", fee);  // 위치는 임의로 "서울"로 설정
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 기존 게시글을 수정하는 메서드
    private void updateEducationPost() {
        String title = titleEditText.getText().toString().trim();  // 제목 가져오기
        String description = descriptionEditText.getText().toString().trim();  // 내용 가져오기
        String category = checkBoxBuy.isChecked() ? "구해요" : checkBoxSell.isChecked() ? "할게요" : "";  // 선택된 카테고리 가져오기
        String feeStr = priceEditText.getText().toString().trim(); // 금액 가져오기
        int fee = feeStr.isEmpty() ? 0 : Integer.parseInt(feeStr); // 금액이 비어 있으면 0으로 설정

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "제목, 내용, 카테고리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();  // 필수 항목 누락 시 경고
            return;
        }

        // 비동기 작업으로 게시글 수정
        new UpdateEducationTask().execute(educationId, title, category, description, "서울", fee);  // 위치는 임의로 "서울"로 설정
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비동기 작업으로 새 게시글을 데이터베이스에 등록하는 클래스
    private class SubmitEducationTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            String title = (String) params[0];
            String category = (String) params[1];
            String description = (String) params[2];
            String location = (String) params[3];
            int fee = (int) params[4];
            int userId = sessionManager.getUserId(); // SessionManager를 통해 사용자 ID 가져오기

            if (userId == -1) {
                Log.e("SubmitEducationTask", "Invalid user ID: " + userId);
                return false; // 사용자 ID가 유효하지 않으면 실패 처리
            }

            try {
                // 현재 시간을 한국 표준시(KST)로 변환
                ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

                Log.d("SubmitEducationTask", "KST Time: " + kstTime);

                // 데이터를 데이터베이스에 삽입
                return educationDAO.insertEducationPost(title, category, description, location, fee, userId, kstTime);
            } catch (Exception e) {
                Log.e("SubmitEducationTask", "Error inserting post", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EducationWriteActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();  // 성공 메시지 표시
                setResult(RESULT_OK);  // 결과 설정
                finish();  // 액티비티 종료
            } else {
                Toast.makeText(EducationWriteActivity.this, "게시글 등록에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비동기 작업으로 기존 게시글을 데이터베이스에서 수정하는 클래스
    private class UpdateEducationTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            int educationId = (int) params[0];  // 교육 게시글 ID 가져오기
            String title = (String) params[1];  // 제목 가져오기
            String category = (String) params[2];  // 카테고리 가져오기
            String description = (String) params[3];  // 내용 가져오기
            String location = (String) params[4];  // 위치 가져오기
            int fee = (int) params[5]; // 금액 가져오기
            int userId = sessionManager.getUserId(); // SessionManager를 통해 사용자 ID 가져오기

            try {
                // 데이터를 데이터베이스에서 업데이트
                return educationDAO.updateEducationPost(educationId, title, category, description, location, fee, userId);
            } catch (Exception e) {
                Log.e("UpdateEducationTask", "Error updating post", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EducationWriteActivity.this, "게시글이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();  // 성공
                setResult(RESULT_OK);  // 결과 설정
                finish();  // 액티비티 종료
            } else {
                Toast.makeText(EducationWriteActivity.this, "게시글 수정에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
