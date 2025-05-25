package com.example.emsismartpresence;


import java.util.List;

public class Presence {
    private String groupe;
    private String site;
    private String date;
    private List<String> absents;

    public Presence() {
        // Constructeur vide requis pour Firestore
    }

    public Presence(String groupe, String site, String date, List<String> absents) {
        this.groupe = groupe;
        this.site = site;
        this.date = date;
        this.absents = absents;
    }

    // Getters et setters
    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }
    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<String> getAbsents() { return absents; }
    public void setAbsents(List<String> absents) { this.absents = absents; }
}