package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<ScheduleEvent> events;
    private EventClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface EventClickListener {
        void onEventClick(ScheduleEvent event);
        void onEventDelete(ScheduleEvent event);
    }

    public EventsAdapter(List<ScheduleEvent> events, EventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        ScheduleEvent event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView timeTextView;
        private TextView locationTextView;
        private TextView courseTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitleTextView);
            timeTextView = itemView.findViewById(R.id.eventTimeTextView);
            locationTextView = itemView.findViewById(R.id.eventLocationTextView);
            courseTextView = itemView.findViewById(R.id.eventCourseTextView);
        }

        public void bind(ScheduleEvent event, EventClickListener listener) {
            titleTextView.setText(event.getTitle());
            timeTextView.setText(timeFormat.format(event.getStartTime()) + " - " + timeFormat.format(event.getEndTime()));
            locationTextView.setText(event.getLocation() != null ? event.getLocation() : "No location");
            courseTextView.setText(event.getCourseCode() != null ? event.getCourseCode() : "No course");

            itemView.setOnClickListener(v -> listener.onEventClick(event));
            itemView.setOnLongClickListener(v -> {
                listener.onEventDelete(event);
                return true;
            });
        }
    }
}