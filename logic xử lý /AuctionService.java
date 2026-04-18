import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuctionService {
    
    private static final int TRIGGER_WINDOW_SECONDS = 60; 
    private static final int EXTENSION_SECONDS = 120;
    
    // Khởi tạo DAO để gọi Stored Procedure
    private AuctionDAO auctionDAO = new AuctionDAO();

    /**
     * Hàm chính xử lý một lượt đặt giá từ Client.
     */
    public  void processBid(int auctionId, int userId, double bidAmount) {
        // 1. Gọi SQL Procedure từ AuctionDAO
        int statusCode = auctionDAO.placeSingleBid(bidAmount, userId, auctionId);
        
        if (statusCode == 1) { // Nếu đặt giá thành công
            // 2. Thông báo giá mới cho mọi người ngay lập tức
            AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, bidAmount));
            
            // 3. Kiểm tra Anti-sniping
            if (checkAndExtend(auctionId)) {
                AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.TIME_EXTENDED, auctionId, "Thời gian đã được gia hạn!"));
            }
            
            // 4. Gọi Service xử lý Auto-bid (Sẽ dùng PriorityQueue mà chúng ta đã thảo luận, không dùng đệ quy)
            AutoBidService autoBidService = new AutoBidService();
            autoBidService.triggerAutoBids(auctionId, bidAmount, userId);
        } else {
            System.out.println("Đặt giá thất bại. Mã lỗi: " + statusCode);
        }
    }

    /**
     * Kiểm tra và gia hạn thời gian nếu thỏa mãn điều kiện Anti-sniping.
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
                
                if (secondsRemaining > 0 && secondsRemaining <= TRIGGER_WINDOW_SECONDS) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, EXTENSION_SECONDS);
                        updateStmt.setInt(2, auctionId);
                        
                        int rowsAffected = updateStmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra Anti-sniping: " + e.getMessage());
        }
        return false;
    }
}
