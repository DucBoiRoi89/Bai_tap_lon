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
        // FIX: Xử lý an toàn trường hợp createdAt bị NULL từ Database để tránh sập Bot
        if (this.createdAt == null && other.createdAt == null) return 0;
        if (this.createdAt == null) return 1;
        if (other.createdAt == null) return -1;
        
        return this.createdAt.compareTo(other.createdAt);
    }
}
// fille này là khuôn mẫu cho autobid 