package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Display user email
        TextView userName = findViewById(R.id.dashboard_adminName);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userName.setText(email != null ? "Bienvenue, " + email.split("@")[0] : "Bienvenue");


        setupCardClickListeners();
    }

    private void setupCardClickListeners() {
        int[] cardIds = {
                R.id.card_assistant,
                R.id.card_annonces,
                R.id.card_proximite,
                R.id.card_documents,
                R.id.card_planning,
        };

        Class<?>[] activityClasses = {
                Assistant_virtuel.class,
                RattrapagesActivity.class,
                MapsActivity.class,
                DocumentsActivity.class,
                ScheduleActivity.class,

        };

        for (int i = 0; i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            Class<?> activityClass = activityClasses[i];
            card.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, activityClass)));
        }
    }
}
