package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Afficher email utilisateur
        TextView userName = findViewById(R.id.dashboard_adminName);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userName.setText(email != null ? "Bienvenue, " + email.split("@")[0] : "Bienvenue");

        setupCardClickListeners();

        // Ajout du clic sur l'image de profil
        findViewById(R.id.profile_image).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }


    private void setupCardClickListeners() {

        int[] cardIds = {
                R.id.card_presence,
                R.id.card_assistant,
                R.id.card_rattrapage,
                R.id.card_proximite,
                R.id.card_documents,
                R.id.card_planning,

        };

        Class<?>[] activityClasses = {
                PresenceActivity.class,
                Assistant_virtuel.class,
                RattrapagesActivity.class,
                MapsActivity.class,
                DocumentsActivity.class,
                ScheduleActivity.class,
                ProfileActivity.class,

        };

        for (int i = 0; i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            Class<?> activityClass = activityClasses[i];
            card.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, activityClass)));
        }
    }
}