package model;

import java.time.LocalDateTime;

public abstract class Item {
    private String id; 
    private String name;
    private String description;
    private String itemType; 
    
    private LocalDateTime endTime;
    private double currentPrice;

    public Item(String id, String name, String description, String itemType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.itemType = itemType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}