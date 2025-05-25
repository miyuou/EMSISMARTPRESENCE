package com.example.emsismartpresence;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PresenceActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private AutoCompleteTextView groupeAutoComplete, siteAutoComplete;
    private Button choisirDateBtn, chargerBtn, enregistrerBtn;
    private TextView dateTextView;
    private RecyclerView recyclerView;
    private PresenceAdapter adapter;
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<String> groupes = new ArrayList<>();
    private List<String> sites = new ArrayList<>();
    private String selectedDate = "";
    private List<String> existingAbsents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence);

        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        groupeAutoComplete = findViewById(R.id.groupeAutoComplete);
        siteAutoComplete = findViewById(R.id.siteAutoComplete);
        choisirDateBtn = findViewById(R.id.choisirDateBtn);
        dateTextView = findViewById(R.id.dateTextView);
        chargerBtn = findViewById(R.id.chargerBtn);
        enregistrerBtn = findViewById(R.id.enregistrerBtn);
        recyclerView = findViewById(R.id.recyclerViewPresence);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PresenceAdapter(etudiants, existingAbsents);
        recyclerView.setAdapter(adapter);

        // Charger les groupes et sites existants
        chargerGroupesEtSites();

        // Écouteurs d'événements
        choisirDateBtn.setOnClickListener(v -> showDatePickerDialog());
        chargerBtn.setOnClickListener(v -> chargerListePresence());
        enregistrerBtn.setOnClickListener(v -> enregistrerPresences());
    }

    private void chargerGroupesEtSites() {
        // Load from 'groups' collection instead of 'documents'
        db.collection("groups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupes.clear();
                        sites.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String group = doc.getString("name");
                            String site = doc.getString("center");

                            if (!groupes.contains(group)) groupes.add(group);
                            if (!sites.contains(site)) sites.add(site);
                        }

                        // Update dropdowns
                        ArrayAdapter<String> groupeAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, groupes);
                        groupeAutoComplete.setAdapter(groupeAdapter);

                        ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, sites);
                        siteAutoComplete.setAdapter(siteAdapter);
                    }
                });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatage de la date au format YYYY-MM-DD
                    selectedDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dateTextView.setText(selectedDate);

                    // Si les autres champs sont remplis, charger les données
                    if (!groupeAutoComplete.getText().toString().isEmpty() &&
                            !siteAutoComplete.getText().toString().isEmpty()) {
                        chargerListePresence();
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void chargerListePresence() {
        String groupe = groupeAutoComplete.getText().toString().trim();
        String site = siteAutoComplete.getText().toString().trim();

        if (groupe.isEmpty() || site.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner un groupe et un site", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Charger la liste des étudiants
        String docId = groupe + "_" + site;
        db.collection("documents").document(docId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Map<String, String>> etudiantsData = (List<Map<String, String>>) task.getResult().get("etudiants");
                        if (etudiantsData != null) {
                            etudiants.clear();
                            for (Map<String, String> data : etudiantsData) {
                                etudiants.add(new Etudiant(
                                        data.get("id"),
                                        data.get("nom"),
                                        data.get("prenom")
                                ));
                            }

                            // 2. Vérifier s'il y a déjà des présences enregistrées pour cette date
                            String presenceDocId = groupe + "_" + site + "_" + selectedDate;
                            db.collection("presences").document(presenceDocId)
                                    .get()
                                    .addOnCompleteListener(presenceTask -> {
                                        if (presenceTask.isSuccessful() && presenceTask.getResult() != null) {
                                            existingAbsents = (List<String>) presenceTask.getResult().get("absents");
                                        } else {
                                            existingAbsents = new ArrayList<>();
                                        }

                                        // Mettre à jour l'adapter avec les nouvelles données
                                        adapter = new PresenceAdapter(etudiants, existingAbsents);
                                        recyclerView.setAdapter(adapter);
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Aucune liste d'étudiants trouvée pour ce groupe et site", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enregistrerPresences() {
        String groupe = groupeAutoComplete.getText().toString().trim();
        String site = siteAutoComplete.getText().toString().trim();

        if (groupe.isEmpty() || site.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner un groupe et un site", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etudiants.isEmpty()) {
            Toast.makeText(this, "Aucun étudiant à enregistrer", Toast.LENGTH_SHORT).show();
            return;
        }

        String presenceDocId = groupe + "_" + site + "_" + selectedDate;
        List<String> absentsIds = adapter.getAbsentsIds();

        Presence presence = new Presence(groupe, site, selectedDate, absentsIds);

        db.collection("presences").document(presenceDocId)
                .set(presence)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Présences enregistrées avec succès", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de l'enregistrement: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}