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
    private static final int TRIGGER_WINDOW_SECONDS = 60; // 1 phút cuối
    private static final int EXTENSION_SECONDS = 120;     // Gia hạn thêm 2 phút
    
    private final AuctionDAO auctionDAO = new AuctionDAO();
    // Quản lý Lock theo từng ID sản phẩm để không làm nghẽn cả hệ thống
    private static final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    private ReentrantLock getLockForAuction(int itemId) {
        return auctionLocks.computeIfAbsent(itemId, k -> new ReentrantLock());
    }

    public void processBid(int itemId, int userId, double bidAmount) 
            throws InvalidBidException, AuctionClosedException, Exception {
        
        ReentrantLock lock = getLockForAuction(itemId);
        lock.lock(); // Đảm bảo tại một thời điểm chỉ một người được xử lý cho 1 sản phẩm
        
        try {
            // 1. Lấy người đang giữ giá cao nhất trước đó để thông báo outbid
            int previousBidderId = auctionDAO.getHighestBidderId(itemId);
            
            // 2. Thực thi đặt giá trong Database
            int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, itemId);
            
            if (statusCode == 1) {
                // Gửi thông báo realtime cho tất cả mọi người
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, itemId, bidAmount));

                // Thông báo riêng cho người vừa bị vượt giá
                if (previousBidderId != -1 && previousBidderId != userId) {
                    AuctionServer.notifySpecificUser(previousBidderId, 
                        new AuctionEvent(AuctionEvent.Type.OUTBID, itemId, "Bạn đã bị vượt giá tại sản phẩm #" + itemId));
                }

                // 3. Kiểm tra Anti-sniping (Gia hạn nếu đặt giá phút chót)
                if (checkAndExtend(itemId)) {
                    AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, itemId, 
                        "Phát hiện đặt giá phút cuối! Hệ thống tự động gia hạn thêm " + (EXTENSION_SECONDS/60) + " phút."));
                }

                // 4. Kích hoạt Auto-bid (nếu có)
                new AutoBidService().triggerAutoBids(itemId, bidAmount, userId, this);
                
            } else if (statusCode == 0) {
                throw new InvalidBidException("Giá đặt không hợp lệ hoặc thấp hơn giá hiện tại!");
            } else if (statusCode == -1) {
                throw new AuctionClosedException("Sản phẩm đã kết thúc đấu giá hoặc đang bị khóa.");
            } else {
                throw new Exception("Lỗi hệ thống trong quá trình xử lý đặt giá.");
            }
            
        } finally {
            lock.unlock(); // Giải phóng lock cho người tiếp theo
        }
    }

    /**
     * Logic Anti-sniping: Gia hạn thời gian nếu có người đặt giá sát giờ kết thúc.
     */
    private boolean checkAndExtend(int itemId) {
        // Đồng bộ tên bảng ITEMS với AuctionDAO
        String selectQuery = "SELECT end_time FROM ITEMS WHERE item_id = ?";
        String updateQuery = "UPDATE ITEMS SET end_time = DATE_ADD(end_time, INTERVAL ? SECOND) WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, itemId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        Timestamp endTime = rs.getTimestamp("end_time");
                        long diffInSeconds = (endTime.getTime() - System.currentTimeMillis()) / 1000;

                        // Nếu còn dưới 60 giây thì gia hạn
                        if (diffInSeconds > 0 && diffInSeconds <= TRIGGER_WINDOW_SECONDS) {
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, EXTENSION_SECONDS);
                                updateStmt.setInt(2, itemId);
                                return updateStmt.executeUpdate() > 0;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi Anti-sniping: " + e.getMessage());
        }
        return false;
    }
    public AuctionDAO getAuctionDAO() {
    return this.auctionDAO;
}
}