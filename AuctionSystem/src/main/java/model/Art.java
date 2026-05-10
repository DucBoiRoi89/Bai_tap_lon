package model;

public class Art extends Item {
    private String artist;
    private int yearCreated; 

    public Art(String id, String name, String description, String artist, int yearCreated) {
        super(id, name, description, "ART");
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }
}