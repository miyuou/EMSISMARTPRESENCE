package com.example.emsismartpresence;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;


public class Assistant_virtuel extends AppCompatActivity {

    private final String API_KEY = "AIzaSyC3cmh9ysphfzQC1NVA-R8QnU36NP1JeJA";

    private LinearLayout chatContainer;
    private EditText editMessage;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_virtuel);

        chatContainer = findViewById(R.id.chatContainer);
        editMessage = findViewById(R.id.prompt);
        scrollView = findViewById(R.id.scrollView);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String userMessage = editMessage.getText().toString();
            if (!userMessage.isEmpty()) {
                // Ajouter le message de l'utilisateur
                addMessageToChat(userMessage, true);
                editMessage.setText("");
                sendMessageToGemini(userMessage);
            }
        });
    }

    private void addMessageToChat(String message, boolean isUser) {
        runOnUiThread(() -> {
            // Créer la bulle de message
            MaterialCardView cardView = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Alignement différent pour utilisateur/assistant
            if (isUser) {
                params.gravity = Gravity.END;
                cardView.setCardBackgroundColor(getResources().getColor(R.color.user_bubble));
            } else {
                params.gravity = Gravity.START;
                cardView.setCardBackgroundColor(getResources().getColor(R.color.assistant_bubble));
            }

            params.setMargins(0, 8, 0, 8);
            cardView.setLayoutParams(params);
            cardView.setRadius(32); // Coins arrondis
            cardView.setCardElevation(2);

            // Texte du message
            TextView textView = new TextView(this);
            textView.setPadding(24, 16, 24, 16);
            textView.setText(message);
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(
                    isUser ? R.color.user_text : R.color.assistant_text));

            cardView.addView(textView);
            chatContainer.addView(cardView);

            // Faire défiler vers le bas
            final ScrollView scrollView = findViewById(R.id.scrollView);
            if (scrollView != null) {
                scrollView.postDelayed(() -> {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }, 100);
            }

        });
    }

    private void sendMessageToGemini(String message) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", message);
            JSONObject contentItem = new JSONObject();
            contentItem.put("parts", new JSONArray().put(part));
            contents.put(contentItem);
            json.put("contents", contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json"));

        String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBWKd8BpMl9Q2dnJgd2DL7eU-AwSILBYyM";
        Request request = new Request.Builder().url(API_URL).post(body).build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    String text = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                    // Ajouter la réponse de l'assistant
                    addMessageToChat(text, false);
                } else {
                    addMessageToChat("Erreur : " + response.message(), false);
                }
            } catch (IOException e) {
                e.printStackTrace();
                addMessageToChat("Erreur de connexion", false);
            } catch (JSONException e) {
                e.printStackTrace();
                addMessageToChat("Erreur de traitement de la réponse", false);
            }
        }).start();
    }
}