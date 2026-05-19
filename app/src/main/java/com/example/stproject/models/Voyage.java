public class Voyage {

    private String id;
    private String titre;
    private String description;
    private Integer note;

    private String dateDebut;
    private String dateFin;

    private List<POI> listePois;

    public Voyage() {
    }

    public Voyage(String id,
                  String titre,
                  String description,
                  Integer note,
                  String dateDebut,
                  String dateFin,
                  List<POI> listePois) {

        this.id = id;
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.listePois = listePois;
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

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public List<POI> getListePois() {
        return listePois;
    }

    public void setListePois(List<POI> listePois) {
        this.listePois = listePois;
    }
}