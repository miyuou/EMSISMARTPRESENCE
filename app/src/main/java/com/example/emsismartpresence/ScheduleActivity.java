package com.example.emsismartpresence;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private CompactCalendarView calendarView;
    private TextView monthYearTextView;
    private TextView selectedDateTextView;
    private ListView eventsListView;
    private Button addEventButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Date selectedDate;
    private List<String> eventsList = new ArrayList<>();
    private ArrayAdapter<String> eventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        calendarView = findViewById(R.id.calendarView);
        monthYearTextView = findViewById(R.id.monthYearTextView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        eventsListView = findViewById(R.id.eventsListView);
        addEventButton = findViewById(R.id.addEventButton);

        eventsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventsList);
        eventsListView.setAdapter(eventsAdapter);

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDate = dateClicked;
                updateUIForSelectedDate(dateClicked);
                loadEventsForDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                monthYearTextView.setText(monthYearFormat.format(firstDayOfNewMonth));
            }
        });

        selectedDate = new Date();
        monthYearTextView.setText(monthYearFormat.format(selectedDate));
        updateUIForSelectedDate(selectedDate);
        loadEventsForDate(selectedDate);

        findViewById(R.id.prevMonthButton).setOnClickListener(v -> scrollCalendar(-1));
        findViewById(R.id.nextMonthButton).setOnClickListener(v -> scrollCalendar(1));
        addEventButton.setOnClickListener(v -> showAddEventDialog());
    }

    private void scrollCalendar(int direction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendarView.getFirstDayOfCurrentMonth());
        calendar.add(Calendar.MONTH, direction);
        calendarView.setCurrentDate(calendar.getTime());
        monthYearTextView.setText(monthYearFormat.format(calendar.getTime()));
    }

    private void updateUIForSelectedDate(Date date) {
        selectedDateTextView.setText(dateFormat.format(date));
    }

    private void loadEventsForDate(Date date) {
        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateKey = dbDateFormat.format(date);
        eventsList.clear();
        calendarView.removeAllEvents();

        db.collection("professors")
                .document(currentUser.getUid())
                .collection("schedule")
                .whereEqualTo("date", dateKey)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            Date startTimeDate = document.getDate("startTime");
                            Date endTimeDate = document.getDate("endTime");
                            String notes = document.getString("notes");

                            String startTime = startTimeDate != null ? timeFormat.format(startTimeDate) : "??:??";
                            String endTime = endTimeDate != null ? timeFormat.format(endTimeDate) : "??:??";

                            String eventText = courseName + " (" + startTime + " - " + endTime + ")";
                            if (notes != null && !notes.isEmpty()) {
                                eventText += "\nNotes: " + notes;
                            }

                            eventsList.add(eventText);

                            // Ajout d'un point sur le calendrier
                            long timeInMillis = date.getTime();
                            Event calendarEvent = new Event(getResources().getColor(R.color.purple_500), timeInMillis, eventText);
                            calendarView.addEvent(calendarEvent);
                        }
                        eventsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Erreur lors du chargement des événements", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddEventDialog() {
        if (selectedDate == null) {
            Toast.makeText(this, "Sélectionnez une date d'abord", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        EditText courseNameInput = dialogView.findViewById(R.id.courseNameInput);
        Button selectTimeButton = dialogView.findViewById(R.id.selectTimeButton);
        TextView timeRangeText = dialogView.findViewById(R.id.timeRangeText);
        EditText notesInput = dialogView.findViewById(R.id.notesInput);

        final Calendar startTime = Calendar.getInstance();
        final Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        final boolean[] timeSelected = {false};

        selectTimeButton.setOnClickListener(v -> {
            // Choix heure début
            new TimePickerDialog(ScheduleActivity.this, (view, hourOfDay, minute) -> {
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startTime.set(Calendar.MINUTE, minute);

                // Choix heure fin
                new TimePickerDialog(ScheduleActivity.this, (view1, hourOfDay1, minute1) -> {
                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay1);
                    endTime.set(Calendar.MINUTE, minute1);

                    if (endTime.before(startTime)) {
                        Toast.makeText(ScheduleActivity.this, "L'heure de fin doit être après l'heure de début", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    timeSelected[0] = true;
                    timeRangeText.setText("De " + timeFormat.format(startTime.getTime()) + " à " + timeFormat.format(endTime.getTime()));
                }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), true).show();

            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), true).show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un cours");
        builder.setView(dialogView);
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String courseName = courseNameInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (courseName.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom de cours", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!timeSelected[0]) {
                Toast.makeText(this, "Veuillez sélectionner l'heure", Toast.LENGTH_SHORT).show();
                return;
            }

            saveEventToFirestore(courseName, notes, startTime.getTime(), endTime.getTime());
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void saveEventToFirestore(String courseName, String notes, Date startTime, Date endTime) {
        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateKey = dbDateFormat.format(selectedDate);

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("date", dateKey);
        eventMap.put("courseName", courseName);
        eventMap.put("notes", notes);
        eventMap.put("startTime", startTime);
        eventMap.put("endTime", endTime);

        db.collection("professors")
                .document(currentUser.getUid())
                .collection("schedule")
                .add(eventMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Cours ajouté", Toast.LENGTH_SHORT).show();
                    loadEventsForDate(selectedDate);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show());
    }
}
