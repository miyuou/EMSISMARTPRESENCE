package com.example.emsimarkpresence;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;


public class Assistant_virtuel extends AppCompatActivity {

    private final String API_KEY = "AIzaSyC3cmh9ysphfzQC1NVA-R8QnU36NP1JeJA";

    private TextView txtResponse;

    private EditText editMessage;

    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_virtuel);

        txtResponse = findViewById(R.id.geminianswer);
        editMessage = findViewById(R.id.prompt);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v->{
            String userMessage = editMessage.getText().toString();
            if(!userMessage.isEmpty()){
                sendMessageToGemini(userMessage);
            }
        });
    }

    private void sendMessageToGemini(String message){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try{
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text",message);
            JSONObject contentItem = new JSONObject();
            contentItem.put("parts", new JSONArray().put(part));
            contents.put(contentItem);
            json.put("contents", contents);
        }catch(Exception e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json"));
        String API_URL= "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBWKd8BpMl9Q2dnJgd2DL7eU-AwSILBYyM";
        Request request = new Request.Builder().url(API_URL).post(body).build();

        new Thread(()-> {
            try(Response response = client.newCall(request).execute()){
                if(response.isSuccessful()){
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    String text = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                    runOnUiThread(()-> txtResponse.setText(text));
                } else {
                    runOnUiThread(() -> txtResponse.setText("Erreur : " + response.message()));
                }
            } catch (IOException e){
                e.printStackTrace();
                runOnUiThread(()-> txtResponse.setText("Erreur : " + e.getMessage()));
            } catch (JSONException e){
                throw new RuntimeException(e);
            }
        }).start();
    }
}