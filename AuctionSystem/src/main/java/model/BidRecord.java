package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidRecord {
    private String itemName;
    private double bidAmount;
    private LocalDateTime bidTime;

    public BidRecord(String itemName, double bidAmount, LocalDateTime bidTime) {
        this.itemName = itemName;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    public String getItemName() { return itemName; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getBidTime() { return bidTime; }

    public String getFormattedPrice() { 
        return String.format("%,.0f VNĐ", bidAmount); 
    }
    

    public String getFormattedTime() {
        return bidTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}