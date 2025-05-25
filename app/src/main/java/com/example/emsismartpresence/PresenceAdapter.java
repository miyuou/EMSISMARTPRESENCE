package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PresenceAdapter extends RecyclerView.Adapter<PresenceAdapter.PresenceViewHolder> {
    private List<Etudiant> etudiants;
    private List<String> absentsIds;
    private boolean[] isPresent;

    public PresenceAdapter(List<Etudiant> etudiants, List<String> absentsIds) {
        this.etudiants = etudiants;
        this.absentsIds = absentsIds != null ? absentsIds : new ArrayList<>();

        // Initialiser le tableau de présence
        this.isPresent = new boolean[etudiants.size()];
        for (int i = 0; i < etudiants.size(); i++) {
            isPresent[i] = !this.absentsIds.contains(etudiants.get(i).getId());
        }
    }

    @NonNull
    @Override
    public PresenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_presence, parent, false);
        return new PresenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresenceViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.nomTextView.setText(etudiant.getNom());
        holder.prenomTextView.setText(etudiant.getPrenom());
        holder.presenceCheckBox.setChecked(isPresent[position]);

        holder.presenceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPresent[position] = isChecked;
        });
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    public List<String> getAbsentsIds() {
        List<String> absents = new ArrayList<>();
        for (int i = 0; i < etudiants.size(); i++) {
            if (!isPresent[i]) {
                absents.add(etudiants.get(i).getId());
            }
        }
        return absents;
    }

    static class PresenceViewHolder extends RecyclerView.ViewHolder {
        TextView nomTextView, prenomTextView;
        CheckBox presenceCheckBox;

        public PresenceViewHolder(@NonNull View itemView) {
            super(itemView);
            nomTextView = itemView.findViewById(R.id.nomTextView);
            prenomTextView = itemView.findViewById(R.id.prenomTextView);
            presenceCheckBox = itemView.findViewById(R.id.presenceCheckBox);
        }
    }
}