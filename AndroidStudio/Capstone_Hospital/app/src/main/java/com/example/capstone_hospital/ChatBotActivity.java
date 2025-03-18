package com.example.capstone_hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {

    Button btnBack;
    RecyclerView recyclerView;
    EditText etMessage;
    ImageButton btnSend;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json");
    String MY_SECRET_KEY = "API_KEY";
    OkHttpClient client;
    int questionNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        messageList = new ArrayList<>();

        btnBack = (Button) findViewById(R.id.btnBack);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);

        client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        addResponse("안녕하세요. 무엇을 도와드릴까요?\n\n1. 질병에 대한 질문\n2. 생활 습관이나 건강 관리에 대한 질문\n3. 앱에 대한 질문\n4. 그 외");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question = etMessage.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                etMessage.setText("");

                if(questionNum == 0) {
                    if (question.equals("0")) {
                        questionNum = 0;
                        addResponse("안녕하세요. 무엇을 도와드릴까요?\n\n1. 질병에 대한 질문\n2. 생활 습관이나 건강 관리에 대한 질문\n3. 앱에 대한 질문\n4. 그 외");
                    } else if (question.equals("1")) {
                        addResponse("질병에 대해 무엇이 궁금하신가요?");
                        questionNum = 2;
                    } else if (question.equals("2")) {
                        addResponse("생활 습관이나 건강 관리에 대해 무엇이 궁금하신가요?");
                        questionNum = 2;
                    } else if (question.equals("3")) {
                        questionNum = 1;
                        addResponse("앱의 어떤 기능에 대해 궁금하신가요?\n\n1. 비대면 자가 진단\n2. AI 간편 진료\n3. 피부질환 진단\n4. 오늘의 건강뉴스");
                    } else if (question.equals("4")) {
                        addResponse("무엇이 궁금하신가요?");
                        questionNum = 2;
                    } else {
                        addResponse("번호를 입력해주세요.");
                    }
                }

                else if (questionNum == 1) {
                    if (question.equals("0")) {
                        questionNum = 0;
                        addResponse("안녕하세요. 무엇을 도와드릴까요?\n\n1. 질병에 대한 질문\n2. 생활 습관이나 건강 관리에 대한 질문\n3. 앱에 대한 질문\n4. 그 외");
                    } else if (question.equals("1")) {
                        addResponse("1. 비대면 자가 진단\n서울대학교 병원 사이트를 기반으로 질문 데이터를 수집해 제작한 기능입니다. " +
                                "선택한 증상에 맞는 질문을 통해 알맞는 질병에 대한 정보를 알려줍니다. 또한 질병의 진료과목에 해당하는 병원을 위치 기반으로 지도에 띄워서 추천해줍니다.");
                        addResponse("0. 첫 화면으로");
                    } else if (question.equals("2")) {
                        addResponse("2. AI 간편 진료\n현재 페이지에 해당하는 기능으로, 질병 뿐만 아니라 생활 습관이나 건강 관리, 앱에 대한 정보를 제공합니다.");
                        addResponse("0. 첫 화면으로");
                    } else if (question.equals("3")) {
                        addResponse("3. 피부질환 진단\n사진을 업로드하면 수집한 데이터를 기반으로 알맞는 피부 질환을 그래프로 나타내줍니다. " +
                                "그래프를 클릭하면 해당 피부질환에 대한 정보를 볼 수 있습니다.");
                        addResponse("0. 첫 화면으로");
                    } else if (question.equals("4")) {
                        addResponse("4. 오늘의 건강뉴스\n매일 하나씩 올라오는 뉴스를 실시간 크롤링하여 띄워주는 기능입니다. 건강에 대한 정보와 지식을 습득할 수 있습니다.");
                        addResponse("0. 첫 화면으로");
                    } else {
                        addResponse("번호를 입력해주세요.");
                    }
                }

                else if (questionNum == 2){
                    if (question.equals("0")) {
                        questionNum = 0;
                        addResponse("안녕하세요. 무엇을 도와드릴까요?\n\n1. 질병에 대한 질문\n2. 생활 습관이나 건강 관리에 대한 질문\n3. 앱에 대한 질문\n4. 그 외");
                    } else {
                        // 기존 API 호출
                        callAPI(question);
                    }
                }
            }
        });
    }

    void addToChat(String message, String sentBy) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response) {
        if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getMessage().equals("입력중...")) {
            messageList.remove(messageList.size() - 1);
        }
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question) {
        messageList.add(new Message("입력중...", Message.SENT_BY_BOT));

        JSONArray arr = new JSONArray();
        JSONObject baseAi = new JSONObject();
        JSONObject userMsg = new JSONObject();
        try {
            // AI 속성설정
            baseAi.put("role", "user");
            baseAi.put("content", "당신은 도움이 되는 AI 어시스턴트입니다. 질병 정보와 건강 관리, 앱 관련 질문에 답변합니다.");

            // 유저 메세지
            userMsg.put("role", "user");
            userMsg.put("content", question);

            // 질문 유형에 따른 프롬프트
            if (question.equals("1")) {
                baseAi.put("content", "이제 질병에 대한 정보를 제공하고 있습니다. 정확하고 유용한 답변을 해주세요.");
            } else if (question.equals("2")) {
                baseAi.put("content", "이제 생활 습관이나 건강 관리에 대한 질문에 답변하고 있습니다. 정확하고 유용한 답변을 해주세요.");
            } else if (question.equals("3")) {
                baseAi.put("content", "이제 앱의 기능과 관련된 질문에 답변하고 있습니다. 정확하고 유용한 답변을 해주세요.");
            } else if (question.equals("4")) {
                baseAi.put("content", "이제 질병, 생활 습관이나 건강 관리, 증상, 병원 등등 의료와 관련된 질문에 답변하고 있습니다. 정확하고 유용한 답변을 해주세요.");
            }

            // array로 담아서 한번에 보냄
            arr.put(baseAi);
            arr.put(userMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo");
            object.put("messages", arr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");

                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());
                        addResponse("0. 첫 화면으로");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                }
            }
        });
    }
}

class Message {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT = "bot";

    String message;
    String sentBy;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}

class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        MyViewHolder myViewHolder = new MyViewHolder(chatView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        Message previousMessage = position > 0 ? messageList.get(position - 1) : null;

        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.tvRight.setText(message.getMessage());
        } else {
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.tvLeft.setText(message.getMessage());

            if (previousMessage != null && previousMessage.getSentBy().equals(Message.SENT_BY_BOT)) {
                holder.chatBotImg.setVisibility(View.INVISIBLE);
            } else {
                holder.chatBotImg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatView, rightChatView;
        TextView tvLeft, tvRight;
        ImageView chatBotImg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat);
            rightChatView = itemView.findViewById(R.id.right_chat);
            tvLeft = itemView.findViewById(R.id.tvLeft);
            tvRight = itemView.findViewById(R.id.tvRight);
            chatBotImg = itemView.findViewById(R.id.chatBotImg);
        }
    }
}