package service;

import dao.AuctionDAO;
import model.AuctionEvent;
import core.AuctionServer;
import config.DatabaseConnection;
import exception.AuctionClosedException;
import exception.InvalidBidException;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionService {
    private static final int TRIGGER_WINDOW_SECONDS = 60; 
    private static final int EXTENSION_SECONDS = 120;     
    
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private static final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    private ReentrantLock getLockForAuction(int auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());
    }

    public void processBid(int auctionId, int userId, double bidAmount) 
            throws InvalidBidException, AuctionClosedException, Exception {
        
        ReentrantLock lock = getLockForAuction(auctionId);
        lock.lock(); 
        
        try {
            int previousBidderId = auctionDAO.getHighestBidderId(auctionId);
            int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, auctionId);
            
            if (statusCode == 1) {
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, bidAmount));

                if (previousBidderId != -1 && previousBidderId != userId) {
                    AuctionServer.notifySpecificUser(previousBidderId, 
                        new AuctionEvent(AuctionEvent.Type.OUTBID, auctionId, "Bạn đã bị vượt giá tại phiên #" + auctionId));
                }

                if (checkAndExtend(auctionId)) {
                    AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, 
                        "Phát hiện đặt giá phút cuối! Hệ thống tự động gia hạn 2 phút."));
                }

                // Gọi Auto-bid
                new AutoBidService().triggerAutoBids(auctionId, bidAmount, userId, this);
                
            } else if (statusCode == 0) {
                throw new InvalidBidException("Giá đặt không hợp lệ hoặc thấp hơn giá hiện tại!");
            } else if (statusCode == -1) {
                throw new AuctionClosedException("Sản phẩm đã kết thúc đấu giá hoặc đang bị khóa.");
            } else if (statusCode == -3) {
                throw new InvalidBidException("Số dư tài khoản của bạn không đủ để giao dịch!");
            } else {
                throw new Exception("Lỗi Database khi đặt giá (Mã lỗi: " + statusCode + ").");
            }
            
        } finally {
            lock.unlock(); 
        }
    }

    private boolean checkAndExtend(int auctionId) {
        // FIX: Cập nhật thời gian vào bảng AUCTIONS chứ không phải ITEMS
        String selectQuery = "SELECT end_time FROM AUCTIONS WHERE auction_id = ?";
        String updateQuery = "UPDATE AUCTIONS SET end_time = DATE_ADD(end_time, INTERVAL ? SECOND) WHERE auction_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, auctionId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        Timestamp endTime = rs.getTimestamp("end_time");
                        long diffInSeconds = (endTime.getTime() - System.currentTimeMillis()) / 1000;

                        if (diffInSeconds > 0 && diffInSeconds <= TRIGGER_WINDOW_SECONDS) {
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, EXTENSION_SECONDS);
                                updateStmt.setInt(2, auctionId);
                                return updateStmt.executeUpdate() > 0;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) { System.err.println("Lỗi Anti-sniping: " + e.getMessage()); }
        return false;
    }
    public AuctionDAO getAuctionDAO() { return this.auctionDAO; }
}