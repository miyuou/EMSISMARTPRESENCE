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
    private AutoCompleteTextView siteInput, filiereInput, anneeInput, groupeInput;
    private EditText nomEtudiant, prenomEtudiant;
    private Button ajouterBtn, sauvegarderBtn, allerPresenceBtn, addGroupBtn;
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<String> sites = new ArrayList<>();
    private List<String> filieres = new ArrayList<>();
    private List<String> annees = new ArrayList<>();
    private List<String> groupes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupDropdownListeners();
        loadInitialSites();
    }

    private void initViews() {
        siteInput = findViewById(R.id.site_input);
        filiereInput = findViewById(R.id.filiere_input);
        anneeInput = findViewById(R.id.annee_input);
        groupeInput = findViewById(R.id.groupe_input);
        nomEtudiant = findViewById(R.id.nomEtudiant);
        prenomEtudiant = findViewById(R.id.prenomEtudiant);
        ajouterBtn = findViewById(R.id.ajouterBtn);
        sauvegarderBtn = findViewById(R.id.sauvegarderBtn);
        allerPresenceBtn = findViewById(R.id.allerPresenceBtn);
        addGroupBtn = findViewById(R.id.addGroupBtn);
        recyclerView = findViewById(R.id.recyclerViewEtudiants);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EtudiantAdapter(etudiants, position -> {
            etudiants.remove(position);
            adapter.notifyItemRemoved(position);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupDropdownListeners() {
        // Initialize all dependent fields as disabled
        filiereInput.setEnabled(false);
        anneeInput.setEnabled(false);
        groupeInput.setEnabled(false);

        // Site selection listener
        siteInput.setOnItemClickListener((parent, view, position, id) -> {
            String site = (String) parent.getItemAtPosition(position);
            filiereInput.setEnabled(true); // Enable filiere after site is selected
            loadFilieresForSite(site);
            resetDependentFields(filiereInput, anneeInput, groupeInput);
        });

        // Filière selection listener
        filiereInput.setOnItemClickListener((parent, view, position, id) -> {
            String filiere = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            if (!site.isEmpty()) {
                anneeInput.setEnabled(true); // Enable année after filiere is selected
                loadAnneesForFiliere(site, filiere);
                resetDependentFields(anneeInput, groupeInput);
            }
        });

        // Année selection listener
        anneeInput.setOnItemClickListener((parent, view, position, id) -> {
            String annee = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            String filiere = filiereInput.getText().toString();
            if (!site.isEmpty() && !filiere.isEmpty()) {
                groupeInput.setEnabled(true); // Enable groupe after année is selected
                loadGroupesForAnnee(site, filiere, annee);
                resetDependentFields(groupeInput);
            }
        });
        ajouterBtn.setOnClickListener(v -> ajouterEtudiant());
        sauvegarderBtn.setOnClickListener(v -> sauvegarderListe());
        allerPresenceBtn.setOnClickListener(v -> startActivity(new Intent(this, PresenceActivity.class)));
        addGroupBtn.setOnClickListener(v -> startActivity(new Intent(this, AddGroupActivity.class)));
    }
    private void resetDependentFields(AutoCompleteTextView... fields) {
        runOnUiThread(() -> {
            for (AutoCompleteTextView field : fields) {
                if (field != null) {
                    field.setText("");
                    field.setEnabled(false);
                    // Instead of setting adapter to null, create empty adapter
                    field.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<>()));
                }
            }
            etudiants.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void loadInitialSites() {
        db.collection("groups")
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> uniqueSites = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        uniqueSites.add(doc.getString("site"));
                    }
                    sites = new ArrayList<>(uniqueSites);
                    updateDropdown(siteInput, sites);
                });
    }

    private void loadFilieresForSite(String site) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> uniqueFilieres = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        uniqueFilieres.add(doc.getString("filiere"));
                    }
                    filieres = new ArrayList<>(uniqueFilieres);
                    updateDropdown(filiereInput, filieres);
                });
    }

    private void loadAnneesForFiliere(String site, String filiere) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .whereEqualTo("filiere", filiere)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> uniqueAnnees = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        uniqueAnnees.add(doc.getString("annee"));
                    }
                    annees = new ArrayList<>(uniqueAnnees);
                    updateDropdown(anneeInput, annees);
                });
    }

    private void loadGroupesForAnnee(String site, String filiere, String annee) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .whereEqualTo("filiere", filiere)
                .whereEqualTo("annee", annee)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> uniqueGroupes = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        uniqueGroupes.add(doc.getString("groupe"));
                    }
                    groupes = new ArrayList<>(uniqueGroupes);
                    updateDropdown(groupeInput, groupes);
                });
    }



    private void updateDropdown(AutoCompleteTextView dropdown, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, items);
        dropdown.setAdapter(adapter);
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

        nomEtudiant.setText("");
        prenomEtudiant.setText("");
    }

    private void sauvegarderListe() {
        String site = siteInput.getText().toString().trim();
        String filiere = filiereInput.getText().toString().trim();
        String annee = anneeInput.getText().toString().trim();
        String groupe = groupeInput.getText().toString().trim();

        if (site.isEmpty() || filiere.isEmpty() || annee.isEmpty() || groupe.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etudiants.isEmpty()) {
            Toast.makeText(this, "Aucun étudiant à sauvegarder", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = generateCompositeId(site, filiere, annee, groupe);

        List<Map<String, String>> etudiantsData = new ArrayList<>();
        for (Etudiant etudiant : etudiants) {
            Map<String, String> data = new HashMap<>();
            data.put("id", etudiant.getId());
            data.put("nom", etudiant.getNom());
            data.put("prenom", etudiant.getPrenom());
            etudiantsData.add(data);
        }

        Map<String, Object> document = new HashMap<>();
        document.put("site", site);
        document.put("filiere", filiere);
        document.put("annee", annee);
        document.put("groupe", groupe);
        document.put("etudiants", etudiantsData);

        db.collection("documents").document(docId)
                .set(document)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Liste sauvegardée avec succès", Toast.LENGTH_SHORT).show();
                    updateDropdowns(site, filiere, annee, groupe);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateCompositeId(String site, String filiere, String annee, String groupe) {
        return String.format("%s_%s_%s_%s",
                site.replace(" ", "_"),
                filiere.replace(" ", "_"),
                annee.replace(" ", "_"),
                groupe.replace(" ", "_"));
    }

    private void updateDropdowns(String site, String filiere, String annee, String groupe) {
        if (!sites.contains(site)) {
            sites.add(site);
            updateDropdown(siteInput, sites);
        }
        if (!filieres.contains(filiere)) {
            filieres.add(filiere);
            updateDropdown(filiereInput, filieres);
        }
        if (!annees.contains(annee)) {
            annees.add(annee);
            updateDropdown(anneeInput, annees);
        }
        if (!groupes.contains(groupe)) {
            groupes.add(groupe);
            updateDropdown(groupeInput, groupes);
        }
    }
}