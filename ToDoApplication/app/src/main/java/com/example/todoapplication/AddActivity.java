package com.example.todoapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();
    EditText titleEditText, contentEditText;
    Button clickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        titleEditText = findViewById(R.id.title);
        contentEditText = findViewById(R.id.content);
        clickButton = findViewById(R.id.add_button);

        titleEditText.setOnFocusChangeListener(this);
        }

    public void save(View v) {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String key = reference.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

            if (title.matches("")) {
                titleEditText.setError("入力してください！！");
                //ボタンを無効にする処理
                clickButton.setEnabled(false);
                Toast.makeText(this, "何も入力されていません。", Toast.LENGTH_SHORT).show();
                return;
        }else{
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String str = titleEditText.getText().toString().trim();

            if(hasFocus) {
                    //ボタンを有効にする処理
                    clickButton.setEnabled(true);
            }
    }
}