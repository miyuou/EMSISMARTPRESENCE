package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private CompactCalendarView calendarView;
    private TextView selectedDateTextView;
    private TextView eventsTextView;
    private EditText notesEditText;
    private Button saveNoteButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Date selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        calendarView = findViewById(R.id.calendarView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        eventsTextView = findViewById(R.id.eventsTextView);
        notesEditText = findViewById(R.id.notesEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);

        // Configuration du calendrier
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.setCurrentDate(new Date());
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDate = dateClicked;
                updateUIForSelectedDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                selectedDateTextView.setText(monthFormat.format(firstDayOfNewMonth));
            }
        });

        selectedDate = new Date();
        updateUIForSelectedDate(selectedDate);

        saveNoteButton.setOnClickListener(v -> saveNote());
    }

    private void updateUIForSelectedDate(Date date) {
        selectedDateTextView.setText(dateFormat.format(date));
        displayEventsForDate(date);
        loadNoteForDate(date);
    }

    private void displayEventsForDate(Date date) {
        List<Event> events = calendarView.getEvents(date);
        StringBuilder eventsText = new StringBuilder("Événements:\n");

        if (events != null && !events.isEmpty()) {
            for (Event event : events) {
                eventsText.append("• ").append(event.getData()).append("\n");
            }
        } else {
            eventsText.append("Aucun événement prévu");
        }
        eventsTextView.setText(eventsText.toString());
    }

    private void loadNoteForDate(Date date) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String dateKey = dbDateFormat.format(date);
            db.collection("professors")
                    .document(user.getUid())
                    .collection("notes")
                    .document(dateKey)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            notesEditText.setText(document.getString("note"));
                        }
                    });
        }
    }

    private void saveNote() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && selectedDate != null) {
            String noteText = notesEditText.getText().toString().trim();
            String dateKey = dbDateFormat.format(selectedDate);

            Map<String, Object> noteData = new HashMap<>();
            noteData.put("note", noteText);
            noteData.put("date", dateKey);

            DocumentReference noteRef = db.collection("professors")
                    .document(user.getUid())
                    .collection("notes")
                    .document(dateKey);

            if (noteText.isEmpty()) {
                noteRef.delete().addOnSuccessListener(aVoid -> updateCalendarMarker(false));
            } else {
                noteRef.set(noteData).addOnSuccessListener(aVoid -> {
                    updateCalendarMarker(true);
                    Toast.makeText(this, "Note enregistrée", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void updateCalendarMarker(boolean hasNote) {
        calendarView.removeEvents(selectedDate);
        if (hasNote) {
            Event event = new Event(ContextCompat.getColor(this, R.color.purple_700),
                    selectedDate.getTime(), "Note");
            calendarView.addEvent(event);
        }
    }
}