package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DocumentsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private AutoCompleteTextView groupeAutoComplete, siteAutoComplete;
    private EditText nomEtudiant, prenomEtudiant;
    private Button ajouterBtn, sauvegarderBtn, allerPresenceBtn;
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<String> groupes = new ArrayList<>();
    private List<String> sites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        groupeAutoComplete = findViewById(R.id.groupeAutoComplete);
        siteAutoComplete = findViewById(R.id.siteAutoComplete);
        nomEtudiant = findViewById(R.id.nomEtudiant);
        prenomEtudiant = findViewById(R.id.prenomEtudiant);
        ajouterBtn = findViewById(R.id.ajouterBtn);
        sauvegarderBtn = findViewById(R.id.sauvegarderBtn);
        allerPresenceBtn = findViewById(R.id.allerPresenceBtn);
        recyclerView = findViewById(R.id.recyclerViewEtudiants);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EtudiantAdapter(etudiants, position -> {
            etudiants.remove(position);
            adapter.notifyItemRemoved(position);
        });
        recyclerView.setAdapter(adapter);

        // Charger les groupes et sites existants
        chargerGroupesEtSites();

        // Écouteurs d'événements
        ajouterBtn.setOnClickListener(v -> ajouterEtudiant());
        sauvegarderBtn.setOnClickListener(v -> sauvegarderListe());
        allerPresenceBtn.setOnClickListener(v -> startActivity(new Intent(DocumentsActivity.this, PresenceActivity.class)));
        Button addGroupBtn = findViewById(R.id.addGroupBtn);
        addGroupBtn.setOnClickListener(v -> {
            startActivity(new Intent(DocumentsActivity.this, AddGroupActivity.class));
        });
        // Écoute des changements pour charger la liste correspondante
        groupeAutoComplete.setOnItemClickListener((parent, view, position, id) -> chargerListeEtudiants());
        siteAutoComplete.setOnItemClickListener((parent, view, position, id) -> chargerListeEtudiants());
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
    private void chargerListeEtudiants() {
        String groupe = groupeAutoComplete.getText().toString().trim();
        String site = siteAutoComplete.getText().toString().trim();

        if (!groupe.isEmpty() && !site.isEmpty()) {
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
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void ajouterEtudiant() {
        String nom = nomEtudiant.getText().toString().trim();
        String prenom = prenomEtudiant.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom et un prénom", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        etudiants.add(new Etudiant(id, nom, prenom));
        adapter.notifyItemInserted(etudiants.size() - 1);

        // Réinitialiser les champs
        nomEtudiant.setText("");
        prenomEtudiant.setText("");
    }

    private void sauvegarderListe() {
        String groupe = groupeAutoComplete.getText().toString().trim();
        String site = siteAutoComplete.getText().toString().trim();

        if (groupe.isEmpty() || site.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner un groupe et un site", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etudiants.isEmpty()) {
            Toast.makeText(this, "Aucun étudiant à sauvegarder", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = groupe + "_" + site;

        // Convertir la liste d'étudiants en format compatible avec Firestore
        List<Map<String, String>> etudiantsData = new ArrayList<>();
        for (Etudiant etudiant : etudiants) {
            Map<String, String> data = new HashMap<>();
            data.put("id", etudiant.getId());
            data.put("nom", etudiant.getNom());
            data.put("prenom", etudiant.getPrenom());
            etudiantsData.add(data);
        }

        Map<String, Object> document = new HashMap<>();
        document.put("groupe", groupe);
        document.put("site", site);
        document.put("etudiants", etudiantsData);

        db.collection("documents").document(docId)
                .set(document)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Liste sauvegardée avec succès", Toast.LENGTH_SHORT).show();

                    // Mettre à jour les listes de groupes et sites si nécessaire
                    if (!groupes.contains(groupe)) {
                        groupes.add(groupe);
                        ArrayAdapter<String> groupeAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, groupes);
                        groupeAutoComplete.setAdapter(groupeAdapter);
                    }

                    if (!sites.contains(site)) {
                        sites.add(site);
                        ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, sites);
                        siteAutoComplete.setAdapter(siteAdapter);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de la sauvegarde: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}