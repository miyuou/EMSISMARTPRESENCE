package com.example.emsismartpresence;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RattrapagesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout sessionsContainer;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rattrapages);

        db = FirebaseFirestore.getInstance();
        sessionsContainer = findViewById(R.id.sessionsContainer);
        MaterialButton addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> showAddSessionDialog());

        loadSessions();
    }

    private void showAddSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une session");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_session, null);
        builder.setView(dialogView);

        EditText subjectInput = dialogView.findViewById(R.id.subjectInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        Button dateButton = dialogView.findViewById(R.id.dateButton);
        Button timeButton = dialogView.findViewById(R.id.timeButton);

        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String subject = subjectInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            if (subject.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            addSessionToFirestore(subject, calendar.getTime(), location);
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void addSessionToFirestore(String subject, Date date, String location) {
        Map<String, Object> session = new HashMap<>();
        session.put("subject", subject);
        session.put("date", date);
        session.put("location", location);

        db.collection("rattrapages")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Session ajoutée", Toast.LENGTH_SHORT).show();
                    loadSessions();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadSessions() {
        sessionsContainer.removeAllViews();
        db.collection("rattrapages")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addSessionToView(
                                    document.getId(),
                                    document.getString("subject"),
                                    document.getDate("date"),
                                    document.getString("location")
                            );
                        }
                    } else {
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addSessionToView(String id, String subject, Date date, String location) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_session, null);

        TextView sessionText = cardView.findViewById(R.id.sessionText);
        MaterialButton editButton = cardView.findViewById(R.id.editButton);
        MaterialButton deleteButton = cardView.findViewById(R.id.deleteButton);

        String formattedDate = dateFormat.format(date);
        sessionText.setText(String.format("📚 %s | %s | %s", subject, formattedDate, location));

        editButton.setOnClickListener(v -> showEditDialog(id, subject, date, location));
        deleteButton.setOnClickListener(v -> deleteSession(id));

        sessionsContainer.addView(cardView);
    }

    private void showEditDialog(String id, String currentSubject, Date currentDate, String currentLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier la session");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_session, null);
        builder.setView(dialogView);

        EditText subjectInput = dialogView.findViewById(R.id.subjectInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        Button dateButton = dialogView.findViewById(R.id.dateButton);
        Button timeButton = dialogView.findViewById(R.id.timeButton);

        subjectInput.setText(currentSubject);
        locationInput.setText(currentLocation);
        calendar.setTime(currentDate);
        dateButton.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(currentDate));
        timeButton.setText(new SimpleDateFormat("HH:mm", Locale.FRENCH).format(currentDate));

        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String subject = subjectInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            if (subject.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            updateSessionInFirestore(id, subject, calendar.getTime(), location);
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void updateSessionInFirestore(String id, String subject, Date date, String location) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("subject", subject);
        updates.put("date", date);
        updates.put("location", location);

        db.collection("rattrapages").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Session modifiée", Toast.LENGTH_SHORT).show();
                    loadSessions();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteSession(String id) {
        db.collection("rattrapages").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Session supprimée", Toast.LENGTH_SHORT).show();
                    loadSessions();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}