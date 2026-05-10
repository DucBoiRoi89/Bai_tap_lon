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
    public AuctionDAO getAuctionDAO() {
        return auctionDAO;
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
                        new AuctionEvent(AuctionEvent.Type.OUTBID, auctionId, "Bạn đã bị vượt giá!"));
                }
                if (checkAndExtend(auctionId)) {
                    AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, 
                        "Phát hiện đặt giá phút cuối! Phiên đấu giá đã được gia hạn thêm " + EXTENSION_SECONDS + " giây."));
                }

                new AutoBidService().triggerAutoBids(auctionId, bidAmount, userId, this);
                
            } else if (statusCode == 0) {
                throw new InvalidBidException("Giá đặt (" + bidAmount + ") phải cao hơn giá hiện tại!");
            } else if (statusCode == -1) {
                throw new AuctionClosedException("Phiên đấu giá hiện không trong trạng thái cho phép đặt giá.");
            } else {
                throw new Exception("Lỗi hệ thống không xác định khi thực hiện giao dịch.");
            }
            
        } finally {
            lock.unlock();
        }
    }

   // gia hanj phien dau gia
    private boolean checkAndExtend(int auctionId) {
        String selectQuery = "SELECT end_time FROM AUCTIONS WHERE auction_id = ?";
        String updateQuery = "UPDATE AUCTIONS SET end_time = DATE_ADD(end_time, INTERVAL ? SECOND) WHERE auction_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            
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
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi Anti-sniping: " + e.getMessage());
        }
        return false;
    }
}