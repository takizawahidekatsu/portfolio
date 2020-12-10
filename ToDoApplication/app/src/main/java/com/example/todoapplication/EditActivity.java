package com.example.todoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();
    EditText titleEditText, contentEditText;
    public FirebaseUser user;
    public String uid;
    public ListView mListView;

    public CustomAdapter mCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //ログイン情報を取得
        user = FirebaseAuth.getInstance().getCurrentUser();

        //user id = Uid を取得する
        uid = user.getUid();

        reference = database.getReference("users").child(uid);

        titleEditText = findViewById(R.id.title);
        contentEditText = findViewById(R.id.content);

        Intent intent = getIntent();
        String item = intent.getStringExtra("editkey");
        if (item != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    titleEditText = (EditText) dataSnapshot.child("title").getValue();
                    contentEditText = (EditText) dataSnapshot.child("content").getValue();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        String editTitle = titleEditText.getText().toString().trim();

        if (editTitle.equals("")){
            titleEditText.setError("入力してください！！");
        }
    }

    public void save(View v) {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String key = reference.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

//    引数のToDoDataの内容をデータベースに送る。
        ToDoData toDoData = new ToDoData(key, title, content);

        reference.child("users").child(uid).child(key).setValue(toDoData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                finish();
            }
        });
    }
}