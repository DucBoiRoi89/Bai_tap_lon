package com.uet.auction.service;

import com.uet.auction.dao.*; 
import com.uet.auction.model.*; 
import com.uet.auction.core.AuctionServer;
import com.uet.auction.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuctionService {
    
    private static final int TRIGGER_WINDOW_SECONDS = 60; // 60 giây cuối 
    private static final int EXTENSION_SECONDS = 120; // Gia hạn 2 phút
    
    private AuctionDAO auctionDAO = new AuctionDAO();

    /**
     * Hàm chính xử lý một lượt đặt giá từ Client.
     * Liên kết: Database (DAO), Thông báo (Server), và Tự động (AutoBid)
     */
    public void processBid(int auctionId, int userId, double bidAmount) {
        // 1. Xác định người đang dẫn đầu TRƯỚC khi thực hiện lượt bid mới để gửi OUTBID
        int previousBidderId = auctionDAO.getHighestBidderId(auctionId);

        // 2. Thực hiện đặt giá qua SQL Procedure (Xử lý Concurrency an toàn) 
        int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, auctionId);
        
        if (statusCode == 1) { // Đặt giá thành công
            // A. Thông báo giá mới cho toàn bộ những người đang xem (Realtime Update) 
            AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, bidAmount));
            
            // B. Thông báo OUTBID nâng cao: Gửi đích danh cho người vừa bị vượt giá
            if (previousBidderId != -1 && previousBidderId != userId) {
                AuctionEvent outbidMsg = new AuctionEvent(
                    AuctionEvent.Type.OUTBID, 
                    auctionId, 
                    "Bạn đã bị người dùng khác vượt giá!"
                );
                AuctionServer.notifySpecificUser(previousBidderId, outbidMsg);
            }
            
            // C. Kiểm tra và thực hiện Anti-sniping 
            if (checkAndExtend(auctionId)) {
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, "Thời gian đã được gia hạn thêm!"));
            }
            
            // D. Kích hoạt chuỗi Auto-bid dựa trên PriorityQueue 
            AutoBidService autoBidService = new AutoBidService();
            autoBidService.triggerAutoBids(auctionId, bidAmount, userId);
            
        } else {
            // Chức năng 3.1.5: Xử lý lỗi & ngoại lệ 
            System.err.println("Đặt giá thất bại. Mã lỗi: " + statusCode);
        }
    }

    /**
     * Kiểm tra và gia hạn thời gian nếu có bid mới trong 60s cuối (Anti-sniping Algorithm) 
     */
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
                
                // Nếu còn dưới 60 giây, thực hiện gia hạn 
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
