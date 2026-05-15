package model;

import java.time.LocalDateTime;

public abstract class Item {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice; 
    private LocalDateTime endTime;
    public abstract String getCategory();
    private int auctionId;
    public Item(String id, String name, String description, double startingPrice, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice; 
        this.endTime = endTime;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public LocalDateTime getEndTime() { return endTime; }


    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }
    
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}