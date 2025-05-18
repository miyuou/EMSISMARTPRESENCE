package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        Button btnProfile = findViewById(R.id.buttonProfile);
        Button btnSettings = findViewById(R.id.buttonSettings);
        Button btnAI = findViewById(R.id.buttonAI);
        Button btnMap = findViewById(R.id.buttonMap);
        Button btnLogout = findViewById(R.id.buttonLogout);
        Button btnNotification = findViewById(R.id.Notification);


//        btnProfile.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePage.this, .class);
//            startActivity(intent);
//                });
//
//        btnSettings.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePage.this, .class);
//            startActivity(intent);
//        });

        btnAI.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Assistant_virtuel.class);
            startActivity(intent);
        });

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MapsActivity.class);
            startActivity(intent);
        });

//        btnLogout.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePage.this, .class);
//            startActivity(intent);
//        });
//
//        btnNotification.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePage.this, .class);
//            startActivity(intent);
//        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}