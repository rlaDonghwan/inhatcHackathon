package com.example.hackathonproject.Edu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.R;

public class WriteActivity extends AppCompatActivity {
    private EditText titleEditText; // 제목 입력 필드
    private EditText descriptionEditText; // 내용 입력 필드
    private Button submitButton; // 올리기 버튼
    private CheckBox checkBoxBuy; // 구해요 체크박스
    private CheckBox checkBoxSell; // 할게요 체크박스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        ImageButton backButton = findViewById(R.id.back_button); // 뒤로가기 버튼
        backButton.setOnClickListener(v -> onBackPressed());

        titleEditText = findViewById(R.id.title_edit_text); // 제목 입력 필드 초기화
        descriptionEditText = findViewById(R.id.content_edit_text); // 내용 입력 필드 초기화
        submitButton = findViewById(R.id.btnSummit); // 올리기 버튼 초기화
        checkBoxBuy = findViewById(R.id.checkbox_buy); // 구해요 체크박스 초기화
        checkBoxSell = findViewById(R.id.checkbox_sell); // 할게요 체크박스 초기화

        // 체크박스 상태 변경 리스너 설정
        checkBoxBuy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxSell.setChecked(false);
            }
        });

        checkBoxSell.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxBuy.setChecked(false);
            }
        });

        submitButton.setOnClickListener(v -> submitEducation()); // 버튼 클릭 리스너 설정
    }

    private void submitEducation() {
        String title = titleEditText.getText().toString().trim(); // 제목 입력 값 가져오기
        String description = descriptionEditText.getText().toString().trim(); // 내용 입력 값 가져오기
        String category = checkBoxBuy.isChecked() ? "구해요" : checkBoxSell.isChecked() ? "할게요" : ""; // 체크된 카테고리 가져오기

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "제목, 내용, 카테고리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show(); // 경고 메시지 표시
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.insertEducationPostAsync(title, category, description, "서울", new DatabaseHelper.DatabaseCallback() { // 여기서 위치를 설정하세요
            @Override
            public void onQueryComplete(Object result) {
                boolean success = (boolean) result;
                if (success) {
                    Toast.makeText(WriteActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show(); // 성공 메시지 표시
                    setResult(RESULT_OK); // RESULT_OK 설정
                    finish(); // 현재 액티비티를 종료하고 이전 화면으로 돌아감
                } else {
                    Toast.makeText(WriteActivity.this, "게시글 등록에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show(); // 실패 메시지 표시
                }
            }
        });
    }
}
