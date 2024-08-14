package com.example.hackathonproject.Chat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    TextView postTitle, postDetails, postLocation;
    ImageView newMessageIcon;

    ChatViewHolder(View itemView) {
        super(itemView);
        postTitle = itemView.findViewById(R.id.postTitle);
        postDetails = itemView.findViewById(R.id.postDetails);
        postLocation = itemView.findViewById(R.id.postLocation);
        newMessageIcon = itemView.findViewById(R.id.newMessageIcon); // 이 부분 수정
    }
}
