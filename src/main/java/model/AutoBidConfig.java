package model;
import java.sql.Timestamp;
public class AutoBidConfig implements Comparable<AutoBidConfig> {
    private int configId;
    private int auctionId;
    private int userId;
    private double maxBidAmount;
    private double increment;
    private Timestamp createdAt;
    public AutoBidConfig(int configId, int auctionId, int userId, double maxBidAmount, double increment, Timestamp createdAt) {
        this.configId = configId;
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxBidAmount = maxBidAmount;
        this.increment = increment;
        this.createdAt = createdAt;
    }
    public int getConfigId() { 
        return configId; 
    }

    public int getAuctionId() { 
        return auctionId; 
    }

    public int getUserId() { 
        return userId; 
    }

    public double getMaxBidAmount() { 
        return maxBidAmount; 
    }

    public double getIncrement() { 
        return increment; 
    }

    public Timestamp getCreatedAt() { 
        return createdAt; 
    }
    @Override
    public int compareTo(AutoBidConfig other) {
        return this.createdAt.compareTo(other.createdAt);
    }
}
