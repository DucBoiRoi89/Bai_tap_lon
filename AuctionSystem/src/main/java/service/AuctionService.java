package service;

import dao.AuctionDAO;
import model.AuctionEvent;
import core.AuctionServer;
import config.DatabaseConnection;
import exception.AuctionClosedException;
import exception.InvalidBidException;
import java.sql.*;

public class AuctionService {  
    private static final int TRIGGER_WINDOW_SECONDS = 60; 
    private static final int EXTENSION_SECONDS = 120;
    private AuctionDAO auctionDAO = new AuctionDAO();

    public void processBid(int auctionId, int userId, double bidAmount) 
            throws InvalidBidException, AuctionClosedException, Exception {      
        
        int previousBidderId = auctionDAO.getHighestBidderId(auctionId);
        int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, auctionId);        
        
        if (statusCode == 1) { 
            AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, bidAmount));   
            if (previousBidderId != -1 && previousBidderId != userId) {
                AuctionServer.notifySpecificUser(previousBidderId, new AuctionEvent(AuctionEvent.Type.OUTBID, auctionId, "Bạn đã bị người dùng khác vượt giá!"));
            }     
            if (checkAndExtend(auctionId)) {
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, "Thời gian phiên đấu giá đã được gia hạn!"));
            }      
            new AutoBidService().triggerAutoBids(auctionId, bidAmount, userId, this);        
            
        } else if (statusCode == 0) {
            throw new InvalidBidException("Giá bid của bạn quá thấp so với giá hiện tại!");
        } else if (statusCode == -1) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc hoặc chưa mở!");
        } else {
            throw new Exception("Lỗi hệ thống cơ sở dữ liệu khi đặt giá!");
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
                long secondsRemaining = (endTime.getTime() - System.currentTimeMillis()) / 1000;
                if (secondsRemaining > 0 && secondsRemaining <= TRIGGER_WINDOW_SECONDS) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, EXTENSION_SECONDS);
                        updateStmt.setInt(2, auctionId);         
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) { System.err.println("Lỗi Anti-sniping: " + e.getMessage()); }
        return false;
    }
}