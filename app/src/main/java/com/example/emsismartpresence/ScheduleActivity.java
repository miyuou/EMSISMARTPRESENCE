package com.example.emsismartpresence;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emsismartpresence.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ScheduleActivity extends AppCompatActivity {

    private CompactCalendarView calendarView;
    private TextView selectedDateTextView, eventsTextView;
    private EditText notesEditText;
    private Button saveNoteButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Date selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        initializeFirebase();
        initializeViews();
        setupCalendar();
        setupListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        eventsTextView = findViewById(R.id.eventsTextView);
        notesEditText = findViewById(R.id.notesEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);
    }

    private void setupCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2025);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.setCurrentDate(calendar.getTime());

        selectedDate = new Date();
        updateUIForSelectedDate(selectedDate);
    }

    private void setupListeners() {
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

        saveNoteButton.setOnClickListener(v -> saveNoteForSelectedDate());
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
                    .collection("schedule_notes")
                    .document(dateKey)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        notesEditText.setText(documentSnapshot.exists() ?
                                documentSnapshot.getString("note") : "");
                    })
                    .addOnFailureListener(e -> showToast("Erreur de chargement"));
        }
    }

    private void saveNoteForSelectedDate() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && selectedDate != null) {
            String note = notesEditText.getText().toString().trim();
            String dateKey = dbDateFormat.format(selectedDate);

            DocumentReference noteRef = db.collection("professors")
                    .document(user.getUid())
                    .collection("schedule_notes")
                    .document(dateKey);

            if (note.isEmpty()) {
                deleteNote(noteRef);
            } else {
                saveNote(noteRef, note, dateKey);
            }
        }
    }

    private void deleteNote(DocumentReference noteRef) {
        noteRef.delete()
                .addOnSuccessListener(aVoid -> {
                    updateCalendarMarker(false);
                    showToast("Note supprimée");
                })
                .addOnFailureListener(e -> showToast("Erreur de suppression"));
    }

    private void saveNote(DocumentReference noteRef, String note, String dateKey) {
        ScheduleNote scheduleNote = new ScheduleNote(dateKey, note, selectedDate);

        noteRef.set(scheduleNote)
                .addOnSuccessListener(aVoid -> {
                    updateCalendarMarker(true);
                    showToast("Note enregistrée");
                })
                .addOnFailureListener(e -> showToast("Erreur d'enregistrement"));
    }

    private void updateCalendarMarker(boolean hasNote) {
        calendarView.removeEvents(selectedDate);
        if (hasNote) {
            Event event = new Event(getColor(R.color.orbit_purple),
                    selectedDate.getTime(), "Note");
            calendarView.addEvent(event);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static class ScheduleNote {
        private String dateKey;
        private String note;
        private Date date;

        public ScheduleNote() {}

        public ScheduleNote(String dateKey, String note, Date date) {
            this.dateKey = dateKey;
            this.note = note;
            this.date = date;
        }

        public String getDateKey() {
            return dateKey;
        }

        public String getNote() {
            return note;
        }

        public Date getDate() {
            return date;
        }
    }
}