package model;
import java.io.Serializable;

public class AuctionEvent implements Serializable {
    public enum Type { 
        NEW_BID,         
        OUTBID,          
        TIME_EXTENDED,   
        AUCTION_FINISHED,
        ERROR
    }
    private Type type;
    private int auctionId;
    private Object data;

    public AuctionEvent(Type type, int auctionId, Object data) {
        this.type = type;
        this.auctionId = auctionId;
        this.data = data;
    }
    public Type getType() { return type; }
    public int getAuctionId() { return auctionId; }
    public Object getData() { return data; }
}