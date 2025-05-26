package com.example.emsismartpresence;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PresenceActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private AutoCompleteTextView siteInput, filiereInput, anneeInput, groupeInput;
    private Button dateBtn, loadBtn, saveBtn;
    private TextView dateTv;
    private RecyclerView recyclerView;
    private PresenceAdapter adapter;
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<String> absents = new ArrayList<>();
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupDropdowns();
        setupDropdownListeners();
        loadInitialSites();
    }

    private void initViews() {
        siteInput = findViewById(R.id.site_input);
        filiereInput = findViewById(R.id.filiere_input);
        anneeInput = findViewById(R.id.annee_input);
        groupeInput = findViewById(R.id.groupe_input);
        dateBtn = findViewById(R.id.date_btn);
        dateTv = findViewById(R.id.date_tv);
        loadBtn = findViewById(R.id.load_btn);
        saveBtn = findViewById(R.id.save_btn);
        recyclerView = findViewById(R.id.recyclerView);
// Initialize dropdowns as disabled
        filiereInput.setEnabled(false);
        anneeInput.setEnabled(false);
        groupeInput.setEnabled(false);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PresenceAdapter(new ArrayList<>(), new ArrayList<>(), (position, isPresent) -> {
            String studentId = etudiants.get(position).getId();
            if (!isPresent && !absents.contains(studentId)) {
                absents.add(studentId);
            } else if (isPresent) {
                absents.remove(studentId);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupDropdowns() {
        siteInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
        filiereInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
        anneeInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
        groupeInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
    }

    private void setListeners() {
        dateBtn.setOnClickListener(v -> showDatePicker());
        loadBtn.setOnClickListener(v -> loadStudents());
        saveBtn.setOnClickListener(v -> savePresence());

        siteInput.setOnItemClickListener((parent, view, position, id) -> {
            String site = (String) parent.getItemAtPosition(position);
            loadFilieres(site);
            resetDependentFields(filiereInput, anneeInput, groupeInput);
        });

        filiereInput.setOnItemClickListener((parent, view, position, id) -> {
            String filiere = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            loadAnnees(site, filiere);
            resetDependentFields(anneeInput, groupeInput);
        });

        anneeInput.setOnItemClickListener((parent, view, position, id) -> {
            String annee = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            String filiere = filiereInput.getText().toString();
            loadGroupes(site, filiere, annee);
            resetDependentFields(groupeInput);
        });
    }



    private void loadInitialSites() {
        db.collection("groups")
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> sites = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        sites.add(doc.getString("site"));
                    }
                    updateDropdown(siteInput, new ArrayList<>(sites));
                });
    }

    private void loadFilieres(String site) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> filieres = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        filieres.add(doc.getString("filiere"));
                    }
                    updateDropdown(filiereInput, new ArrayList<>(filieres));
                });
    }

    private void loadAnnees(String site, String filiere) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .whereEqualTo("filiere", filiere)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> annees = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        annees.add(doc.getString("annee"));
                    }
                    updateDropdown(anneeInput, new ArrayList<>(annees));
                });
    }

    private void loadGroupes(String site, String filiere, String annee) {
        db.collection("groups")
                .whereEqualTo("site", site)
                .whereEqualTo("filiere", filiere)
                .whereEqualTo("annee", annee)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> groupes = new HashSet<>();
                    for (QueryDocumentSnapshot doc : query) {
                        groupes.add(doc.getString("groupe"));
                    }
                    updateDropdown(groupeInput, new ArrayList<>(groupes));
                });
    }

    private void updateDropdown(AutoCompleteTextView dropdown, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, items);
        dropdown.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, day);
                    dateTv.setText(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadStudents() {
        String site = siteInput.getText().toString().trim();
        String filiere = filiereInput.getText().toString().trim();
        String annee = anneeInput.getText().toString().trim();
        String groupe = groupeInput.getText().toString().trim();

        if (site.isEmpty() || filiere.isEmpty() || annee.isEmpty() || groupe.isEmpty()) {
            Toast.makeText(this, "Sélectionnez tous les critères", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Sélectionnez une date", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = site + "_" + filiere + "_" + annee + "_" + groupe;
        db.collection("documents").document(docId)
                .get()
                .addOnSuccessListener(document -> {
                    List<Etudiant> tempEtudiants = new ArrayList<>();  // Temporary list
                    if (document.exists()) {
                        List<Map<String, String>> studentsData = (List<Map<String, String>>) document.get("etudiants");
                        if (studentsData != null) {
                            for (Map<String, String> data : studentsData) {
                                tempEtudiants.add(new Etudiant(
                                        data.get("id"),
                                        data.get("nom"),
                                        data.get("prenom")
                                ));
                            }
                        }
                    }

                    // Update adapter only after we have all data
                    runOnUiThread(() -> {
                        etudiants.clear();
                        etudiants.addAll(tempEtudiants);

                        if (etudiants.isEmpty()) {
                            Toast.makeText(this, "Aucun étudiant trouvé", Toast.LENGTH_SHORT).show();
                        } else {
                            // Initialize adapter with proper size
                            adapter = new PresenceAdapter(etudiants, new ArrayList<>(), (position, isPresent) -> {
                                String studentId = etudiants.get(position).getId();
                                if (!isPresent && !absents.contains(studentId)) {
                                    absents.add(studentId);
                                } else if (isPresent) {
                                    absents.remove(studentId);
                                }
                            });
                            recyclerView.setAdapter(adapter);

                            // Now load existing presence
                            loadExistingPresence(site, filiere, annee, groupe);
                        }
                    });
                });
    }

    private void loadExistingPresence(String site, String filiere, String annee, String groupe) {
        String presenceId = site + "_" + filiere + "_" + annee + "_" + groupe + "_" + selectedDate;
        db.collection("presences").document(presenceId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        absents = (List<String>) document.get("absents");
                        if (absents == null) absents = new ArrayList<>();
                    } else {
                        absents = new ArrayList<>();
                    }
                    adapter.updateAbsents(absents);
                    adapter.notifyDataSetChanged();
                });
    }

    private void savePresence() {
        String site = siteInput.getText().toString().trim();
        String filiere = filiereInput.getText().toString().trim();
        String annee = anneeInput.getText().toString().trim();
        String groupe = groupeInput.getText().toString().trim();

        if (site.isEmpty() || filiere.isEmpty() || annee.isEmpty() || groupe.isEmpty()) {
            Toast.makeText(this, "Sélectionnez tous les critères", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Sélectionnez une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etudiants.isEmpty()) {
            Toast.makeText(this, "Aucun étudiant à enregistrer", Toast.LENGTH_SHORT).show();
            return;
        }

        String presenceId = site + "_" + filiere + "_" + annee + "_" + groupe + "_" + selectedDate;
        Map<String, Object> presence = new HashMap<>();
        presence.put("site", site);
        presence.put("filiere", filiere);
        presence.put("annee", annee);
        presence.put("groupe", groupe);
        presence.put("date", selectedDate);
        presence.put("absents", absents);
        presence.put("timestamp", System.currentTimeMillis());

        db.collection("presences").document(presenceId)
                .set(presence)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Présence enregistrée", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupDropdownListeners() {
        siteInput.setOnItemClickListener((parent, view, position, id) -> {
            String site = (String) parent.getItemAtPosition(position);
            filiereInput.setEnabled(true);
            loadFilieres(site);
            resetDependentFields(filiereInput, anneeInput, groupeInput);
        });

        filiereInput.setOnItemClickListener((parent, view, position, id) -> {
            String filiere = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            if (!site.isEmpty()) {
                anneeInput.setEnabled(true);
                loadAnnees(site, filiere);
                resetDependentFields(anneeInput, groupeInput);
            }
        });

        anneeInput.setOnItemClickListener((parent, view, position, id) -> {
            String annee = (String) parent.getItemAtPosition(position);
            String site = siteInput.getText().toString();
            String filiere = filiereInput.getText().toString();
            if (!site.isEmpty() && !filiere.isEmpty()) {
                groupeInput.setEnabled(true);
                loadGroupes(site, filiere, annee);
                resetDependentFields(groupeInput);
            }
        });

        dateBtn.setOnClickListener(v -> showDatePicker());
        loadBtn.setOnClickListener(v -> loadStudents());
        saveBtn.setOnClickListener(v -> savePresence());
    }

    private void resetDependentFields(AutoCompleteTextView... fields) {
        runOnUiThread(() -> {
            for (AutoCompleteTextView field : fields) {
                if (field != null) {
                    field.setText("");
                    field.setEnabled(false);
                    field.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<>()
                    ));
                }
            }
            etudiants.clear();
            absents.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }
}