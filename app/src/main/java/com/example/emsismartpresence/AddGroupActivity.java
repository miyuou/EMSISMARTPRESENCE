package com.example.emsismartpresence;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddGroupActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText siteInput, filiereInput, anneeInput, groupeInput;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        db = FirebaseFirestore.getInstance();
        initViews();
        setListeners();
    }

    private void initViews() {
        siteInput = findViewById(R.id.site_input);
        filiereInput = findViewById(R.id.filiere_input);
        anneeInput = findViewById(R.id.annee_input);
        groupeInput = findViewById(R.id.groupe_input);
        saveBtn = findViewById(R.id.save_btn);
    }

    private void setListeners() {
        saveBtn.setOnClickListener(v -> saveGroup());
    }

    private void saveGroup() {
        String site = siteInput.getText().toString().trim();
        String filiere = filiereInput.getText().toString().trim();
        String annee = anneeInput.getText().toString().trim();
        String groupe = groupeInput.getText().toString().trim();

        if (site.isEmpty() || filiere.isEmpty() || annee.isEmpty() || groupe.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> group = new HashMap<>();
        group.put("site", site);
        group.put("filiere", filiere);
        group.put("annee", annee);
        group.put("groupe", groupe);

        db.collection("groups")
                .add(group)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Groupe ajouté", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}