package com.example.stproject.models;

import java.util.List;
import java.util.Map;

public class Voyage {

    private String id;
    private String titre;
    private String description;
    private Integer note;

    // Indique si le voyage est terminé ou encore en cours
    private boolean termine = false;

    public Voyage() {}

    public Voyage(
            String id,
            String titre,
            String description,
            Integer note,
            boolean termine
    ) {

        this.id = id;
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.termine = termine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public boolean isTermine() {
        return termine;
    }

    public void setTermine(boolean termine) {
        this.termine = termine;
    }
}