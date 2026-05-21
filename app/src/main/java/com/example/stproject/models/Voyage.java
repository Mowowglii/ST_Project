package com.example.stproject.models;

import java.util.List;

public class Voyage {

    private String id;
    private String titre;
    private String description;
    private Integer note;
    private List<POI> listePois;
    private List<Photo> listePhotos;
    private List<Path> path= new ArrayList<>();

    public Voyage() {}

    public Voyage(String id, String titre, String description, Integer note,
                   List<POI> listePois, List<Photo> listePhotos,List<Path> path) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.listePois = listePois;
        this.listePhotos = listePhotos;
        this.path = path;


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

    public List<POI> getListePois() {
        return listePois;
    }

    public void setListePois(List<POI> listePois) {
        this.listePois = listePois;
    }

    public List<Photo> getListePhotos() {
        return listePhotos;
    }

    public void setListePhotos(List<Photo> listePhotos) {
        this.listePhotos = listePhotos;
    }

    public List<Path> getPath() { 
        return path;
     }
    public void setPath(List<Path> path) { 
        this.path = path ;
    }
    
}