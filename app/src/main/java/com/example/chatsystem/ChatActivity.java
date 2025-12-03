package com.example.chatsystem; // 修改包名

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // ================= 配置区域 =================
    // 请在 CMD 中输入 ipconfig (Windows) 查看电脑 IPv4 地址并替换此处
    private static final String SERVER_IP = "192.168.1.8";
    private static final int SERVER_PORT = 8888;
    // ===========================================

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> msgList = new ArrayList<>();
    private EditText etContent;
    private Button btnSend, btnEmoji;
    private GridView emojiPanel;

    private Socket socket;
    private PrintWriter out;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String myUserId; // 简单生成一个随机ID

    // 用于在子线程收到消息后更新 UI
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // 更新列表
                adapter.notifyItemInserted(msgList.size() - 1);
                recyclerView.scrollToPosition(msgList.size() - 1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myUserId = "User_" + System.currentTimeMillis() % 1000;
        initView();
        initDatabase();
        initEmojiPanel();

        // 开启子线程连接服务器
        new Thread(this::connectServer).start();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        etContent = findViewById(R.id.et_content);
        btnSend = findViewById(R.id.btn_send);
        btnEmoji = findViewById(R.id.btn_emoji);
        emojiPanel = findViewById(R.id.panel_emoji);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(msgList);
        recyclerView.setAdapter(adapter);

        // 发送按钮点击
        btnSend.setOnClickListener(v -> {
            String content = etContent.getText().toString();
            if (!content.isEmpty()) {
                sendMsg(content);
                etContent.setText("");
            }
        });

        // 表情按钮点击 (切换面板显示)
        btnEmoji.setOnClickListener(v -> {
            if (emojiPanel.getVisibility() == View.VISIBLE) {
                emojiPanel.setVisibility(View.GONE);
            } else {
                emojiPanel.setVisibility(View.VISIBLE);
            }
        });
    }

    // 初始化简单的表情面板
    private void initEmojiPanel() {
        String[] emojis = {"😀", "😂", "😍", "😭", "😡", "👍", "👎", "🎉", "🌹", "🍺"};
        ArrayAdapter<String> emojiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emojis);
        emojiPanel.setAdapter(emojiAdapter);

        // 点击表情自动填入输入框
        emojiPanel.setOnItemClickListener((parent, view, position, id) -> {
            String emoji = emojis[position];
            etContent.append(emoji);
        });
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        // 加载历史记录
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(cursor.getColumnIndex("sender"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                msgList.add(new ChatMessage(sender, content, time, type));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void connectServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            // 这里必须使用 UTF-8，否则中文乱码
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            runOnUiThread(() -> Toast.makeText(this, "已连接服务器", Toast.LENGTH_SHORT).show());

            String jsonStr;
            while ((jsonStr = in.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    String sender = json.optString("sender");
                    String content = json.optString("content");
                    String time = json.optString("time");

                    // 判断是自己发的还是别人发的
                    int type = sender.equals(myUserId) ? ChatMessage.TYPE_SENT : ChatMessage.TYPE_RECEIVED;

                    // 如果是自己发的，因为我们在发送时已经本地添加了，所以这里可以选择忽略，
                    // 或者更好的做法是：本地发送时不添加，等服务器回传确认后再显示。
                    // 为了简化逻辑，这里假设服务器回传的消息，如果 Sender 是自己，我们就不重复添加了
                    if (!sender.equals(myUserId)) {
                        handleNewMessage(sender, content, time, type);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "连接服务器失败，请检查IP", Toast.LENGTH_LONG).show());
        }
    }

    private void sendMsg(String content) {
        if (out == null) return;
        new Thread(() -> {
            try {
                String time = new SimpleDateFormat("HH:mm").format(new Date());

                // 1. 构建 JSON 协议
                JSONObject json = new JSONObject();
                json.put("sender", myUserId);
                json.put("content", content);
                json.put("time", time);

                // 2. 发送网络消息
                out.println(json.toString());

                // 3. 本地立即显示并存储
                runOnUiThread(() -> handleNewMessage(myUserId, content, time, ChatMessage.TYPE_SENT));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleNewMessage(String sender, String content, String time, int type) {
        // 保存到数据库
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("content", content);
        values.put("time", time);
        values.put("type", type);
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        // 更新内存列表和 UI
        msgList.add(new ChatMessage(sender, content, time, type));
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {}
    }
}