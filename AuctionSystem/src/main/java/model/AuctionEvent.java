package model;

public class AuctionEvent {
    public enum Type {
        NEW_BID, 
        OUTBID, 
        TIME_EXTENDED, 
        ERROR,               
        AUCTION_FINISHED
    }

    private Type type;
    private int itemId; 
    private Object data;

    public AuctionEvent(Type type, int itemId, Object data) {
        this.type = type;
        this.itemId = itemId;
        this.data = data;
    }

    public Type getType() { return type; }
    public int getItemId() { return itemId; }
    public Object getData() { return data; }
}