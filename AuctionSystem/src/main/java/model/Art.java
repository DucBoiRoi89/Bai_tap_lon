package model;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;
    private int yearCreated;

    public Art(String id, String name, String description, double startingPrice, 
               LocalDateTime endTime, String artist, int yearCreated) {
        super(id, name, description, startingPrice, endTime);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    public String getArtist() { return artist; }
    public int getYearCreated() { return yearCreated; }
    @Override
public String getCategory() { return "ART"; }
}