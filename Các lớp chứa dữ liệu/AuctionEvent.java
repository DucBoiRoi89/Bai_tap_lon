package com.uet.auction.model;
import java.io.Serializable;

public class AuctionEvent implements Serializable {
    // Thêm loại OUTBID để thông báo riêng cho người bị vượt giá
    public enum Type { 
        NEW_BID,          // Có giá mới (Gửi cho tất cả)
        OUTBID,           // Bạn đã bị vượt giá (Gửi đích danh)
        TIME_EXTENDED,    // Gia hạn thời gian (Gửi cho tất cả)
        AUCTION_FINISHED  // Kết thúc phiên (Gửi cho tất cả)
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
