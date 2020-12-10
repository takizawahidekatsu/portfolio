package com.example.todoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.internal.ContextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ToDoActivity extends AppCompatActivity implements ListView.OnItemLongClickListener{
    public FirebaseUser user;
    public String uid;
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public DatabaseReference reference;
    public CustomAdapter mCustomAdapter;
    public ListView mListView;
    public final static String EXTRA_MYID = "com.example.todoapplication.MYID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        //ログイン情報を取得
        user = FirebaseAuth.getInstance().getCurrentUser();

        //user id = Uid を取得する
        uid = user.getUid();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(uid);

        mListView = (ListView) findViewById(R.id.list_view);
        TextView title = (TextView) findViewById(R.id.title_text_view);

        //CustomAdapter作成して、ListViewにセット
        mCustomAdapter = new CustomAdapter(getApplicationContext(), R.layout.card_view, new ArrayList<ToDoData>());
        mListView.setAdapter(mCustomAdapter);

        //OnClickListenerを設定
        mListView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int[] selected = {0};
                final String[] items = {"赤色","青色","黄色"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ToDoActivity.this);
                builder.setTitle("項目のタグカラーを選択")
                        //タグのカラー選択リストを生成
                        .setSingleChoiceItems(items, selected[0],
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selected[0] = which;
                                    }
                                })
                        //[OK]ボタンを生成
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText( ToDoActivity.this,
                                        String.format("[%s]が選択されました。",items[selected[0]]),
                                        Toast.LENGTH_SHORT).show();
                                //選択されたリスト項目を取得
                                View item = mListView.getChildAt(position);
                                switch (items[selected[0]]){
                                    case "黄色":
                                        item.setBackgroundColor(Color.YELLOW);
                                        break;
                                    case "赤色":
                                        item.setBackgroundColor(Color.RED);
                                        break;
                                    case "青色":
                                        item.setBackgroundColor(Color.BLUE);
                                        break;
                                }
                            }
                        })
                        //[キャンセル]ボタンを生成
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
            }
        });

        //LongClickListenerを設定
        mListView.setOnItemLongClickListener(this);

        //firebaseと同期するリスナー
        reference.addChildEventListener(new ChildEventListener() {
            //            データを読み込むときはイベントリスナーを登録して行う。
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                アイテムのリストを取得するか、アイテムのリストへの追加がないかリッスンします。
                ToDoData toDoData = dataSnapshot.getValue(ToDoData.class);
                mCustomAdapter.add(toDoData);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                リスト内のアイテムに対する変更がないかリッスンします。
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                リストから削除されるアイテムがないかリッスンします。
                Log.d("ToDoActivity", "onChildRemoved:" + dataSnapshot.getKey());
                ToDoData result = dataSnapshot.getValue(ToDoData.class);
                if (result == null) return;

                ToDoData item = mCustomAdapter.getToDoDataKey(result.getFirebaseKey());

                mCustomAdapter.remove(item);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                並べ替えリストの項目順に変更がないかリッスンします。
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                ログを記録するなどError時の処理を記載する。
            }
        });
    }
    //新規登録ボタン押下時動作
    public void addButton(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    //編集ボタン押下時動作
    public void editButton(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ToDoData toDoData = mCustomAdapter.getItem(position);
        uid = user.getUid();

        new AlertDialog.Builder(this)
                .setTitle("選択項目完了確認")
                .setMessage("この項目を完了しましたか？\n　Yes:削除　No:キャンセル")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        reference.child(toDoData.getFirebaseKey()).removeValue();
//                        mCustomAdapter.remove(toDoData);
                    }
                })
                .setNegativeButton("No", null)
                .show();
        return true;
    }
    //ユーザーのログアウト
    public void logout(View v) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent intent = new Intent(ToDoActivity.this, LoginActivity.class);
        intent.putExtra("check", true);
        startActivity(intent);
        finish();
    }
}