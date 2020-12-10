package com.example.todoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText emailFormEditText, passwordFormEditText;
    public Intent data;
    public FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailFormEditText = (EditText) findViewById(R.id.email_log_in_edit_text);
        passwordFormEditText = (EditText) findViewById(R.id.password_log_in_edit_text);

        mAuth = FirebaseAuth.getInstance();
    }

    public boolean checkEmpty() {
        if (TextUtils.isEmpty(emailFormEditText.getText())) {
            Log.d("MainActivity", "何も記入されていません");
            return false;
        }

        if (TextUtils.isEmpty(passwordFormEditText.getText())) {
            Log.d("MainActivity", "何も記入されていません");
            return false;
        }
        return true;
    }

    public void loginMailButton(View v) {
        signIn(emailFormEditText.getText().toString(), passwordFormEditText.getText().toString());
        setResult(RESULT_OK, data);
    }

    public void addMailButton(View v) {
        createAccount(emailFormEditText.getText().toString(), passwordFormEditText.getText().toString());
        setResult(RESULT_OK, data);
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);

        if (!checkEmpty()) {
            return;
        }

        //メールアドレスとパスワード受け取りにより、新規ユーザー登録
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // ログインに成功したら、ログインしたユーザーの情報でUIを更新します。
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(MainActivity.this, "新規作成に成功しました！", Toast.LENGTH_SHORT).show();
                            changeActivity();
                        } else {
                            // サインインに失敗した場合は、ユーザーにメッセージを表示します。
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "サインインに失敗しました！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        if (!checkEmpty()) {
            return;
        }

        //メールアドレスとパスワード受け取りにより、既存のユーザーログイン
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // ログインに成功したら、ログインしたユーザーの情報でUIを更新します。
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(MainActivity.this, "ログインに成功しました！", Toast.LENGTH_SHORT).show();
                            changeActivity();
                        } else {
                            // サインインに失敗した場合は、ユーザーにメッセージを表示します。
                            Log.w(TAG, "サインインに失敗しました！", task.getException());

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(task.getException().getMessage())
                                    .setTitle("Error!")
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                    }
                });
    }
    //リスト画面に遷移するメソッド
    private void changeActivity() {
        Intent intent = new Intent(this, ToDoActivity.class);
        startActivity(intent);
        finish();
    }

}