package service;
import dao.*;
import model.*;
import core.AuctionServer;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
public class AuctionService {  
    private static final int TRIGGER_WINDOW_SECONDS = 60; 
    private static final int EXTENSION_SECONDS = 120;
    
    private AuctionDAO auctionDAO = new AuctionDAO();
    public void processBid(int auctionId, int userId, double bidAmount) {
        int previousBidderId = auctionDAO.getHighestBidderId(auctionId);
        int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, auctionId);  
        if (statusCode == 1) { 
            AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, bidAmount));
            if (previousBidderId != -1 && previousBidderId != userId) {
                AuctionEvent outbidMsg = new AuctionEvent(
                    AuctionEvent.Type.OUTBID, 
                    auctionId, 
                    "Bạn đã bị người dùng khác vượt giá!"
                );
                AuctionServer.notifySpecificUser(previousBidderId, outbidMsg);
            }
            if (checkAndExtend(auctionId)) {
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, "Thời gian đã được gia hạn thêm!"));
            }
            AutoBidService autoBidService = new AutoBidService();
            autoBidService.triggerAutoBids(auctionId, bidAmount, userId);
            
        } else {
            System.err.println("Đặt giá thất bại. Mã lỗi: " + statusCode);
        }
    }
    public boolean checkAndExtend(int auctionId) {
        String selectQuery = "SELECT end_time FROM AUCTIONS WHERE auction_id = ?";
        String updateQuery = "UPDATE AUCTIONS SET end_time = DATE_ADD(end_time, INTERVAL ? SECOND) WHERE auction_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {     
            selectStmt.setInt(1, auctionId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                Timestamp endTime = rs.getTimestamp("end_time");
                long currentTimeMillis = System.currentTimeMillis();
                long endTimeMillis = endTime.getTime();         
                long secondsRemaining = (endTimeMillis - currentTimeMillis) / 1000;
                if (secondsRemaining > 0 && secondsRemaining <= TRIGGER_WINDOW_SECONDS) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, EXTENSION_SECONDS);
                        updateStmt.setInt(2, auctionId);         
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thực thi Anti-sniping: " + e.getMessage());
        }
        return false;
    }
}
