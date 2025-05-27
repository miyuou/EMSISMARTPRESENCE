package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private List<Etudiant> etudiants;
    private OnEtudiantClickListener listener;

    public interface OnEtudiantClickListener {
        void onDeleteClick(int position);
    }

    public EtudiantAdapter(List<Etudiant> etudiants, OnEtudiantClickListener listener) {
        this.etudiants = etudiants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.nomTextView.setText(etudiant.getNom());
        holder.prenomTextView.setText(etudiant.getPrenom());
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
        notifyDataSetChanged();
    }

    static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView nomTextView, prenomTextView;
        MaterialButton deleteButton;

        public EtudiantViewHolder(@NonNull View itemView, OnEtudiantClickListener listener) {
            super(itemView);
            nomTextView = itemView.findViewById(R.id.nomTextView);
            prenomTextView = itemView.findViewById(R.id.prenomTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }
}
