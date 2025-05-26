package com.example.emsismartpresence;
public class Etudiant {
    private String id;
    private String nom;
    private String prenom;

    public Etudiant() {
        // Constructeur vide requis pour Firestore
    }

    public Etudiant(String id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}