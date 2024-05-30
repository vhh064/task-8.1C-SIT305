package com.example.a81c;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends AppCompatActivity {

    EditText et_message_input;
    ImageView button_send;
    String name;
    List<String[]> messageList;
    RecyclerView rv_chat;
    private RequestQueue requestQueue;

    JSONArray chatHistoryArray;

    MessageAdapter msgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_window);
        et_message_input = findViewById(R.id.et_message_input);
        et_message_input.setInputType(InputType.TYPE_NULL);
        button_send = findViewById(R.id.button_send);
        rv_chat = findViewById(R.id.rv_chat);
        name = getIntent().getStringExtra("name");
        chatHistoryArray = new JSONArray();
        messageList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        msgAdapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_chat.setLayoutManager(layoutManager);
        rv_chat.setAdapter(msgAdapter);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = et_message_input.getText().toString();
                String[] newMsg = new String[]{input, "user"};
                msgAdapter.addItem(newMsg);
                et_message_input.setText("");
                JSONObject payload = createPayload(input);
                sendRequest(ChatWindow.this, "http://10.0.2.2:5000/chat", payload, input);
            }
        });
    }

    private JSONObject createPayload(String query) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("userMessage", query);
            payload.put("chatHistory", chatHistoryArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    private void sendRequest(Context context, String url, JSONObject payload, String query) {
        System.out.println(payload);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String aiResponse = response.getString("message");
                            System.out.println("aiResponse : " + aiResponse);
                            JSONObject chatHistoryItem = new JSONObject();
                            chatHistoryItem.put("User", query);
                            chatHistoryItem.put("Llama", aiResponse);
                            chatHistoryArray.put(chatHistoryItem);
                            String[] newMsg = new String[]{aiResponse, "ai"};
                            msgAdapter.addItem(newMsg);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        getRequestQueue(context).add(jsonObjectRequest);
    }

    private RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.TaskViewHolder> {

        List<String[]> messages;

        public MessageAdapter(List<String[]> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageAdapter.TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageAdapter.TaskViewHolder holder, int position) {
            String[] msg = messages.get(position);
            holder.bind(msg);
        }

        public void addItem(String[] newItem) {
            messages.add(newItem);
            notifyItemInserted(messages.size() - 1);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tv_user_name, tv_message;
            ImageView img_ai;
            CardView cv_text;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                img_ai = itemView.findViewById(R.id.img_ai);
                tv_user_name = itemView.findViewById(R.id.tv_user_name);
                tv_message = itemView.findViewById(R.id.tv_message);
                cv_text = itemView.findViewById(R.id.cv_text);
            }

            public void bind(String[] msg) {
                tv_user_name.setText((name.charAt(0) + "").toUpperCase());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cv_text.getLayoutParams();
                if(msg[1].equals("ai")) {
                    img_ai.setVisibility(View.VISIBLE);
                    tv_user_name.setVisibility(View.INVISIBLE);
                }else{
                    img_ai.setVisibility(View.INVISIBLE);
                    tv_user_name.setVisibility(View.VISIBLE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    cv_text.setLayoutParams(params);
                }
                tv_message.setText(msg[0]);
            }
        }
    }
}