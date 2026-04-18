package com.uet.auction.dao;

import com.uet.auction.config.DatabaseConnection; // Import kết nối DB
import com.uet.auction.model.AutoBidConfig; // Import Model để sử dụng
import com.uet.auction.config.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class AuctionDAO {

    /**
     * Gọi Stored Procedure để thực hiện đặt giá
     * @return statusCode: 1 (Thành công), 0 (Giá thấp), -1 (Phiên đóng), -2 (Lỗi SQL)
     */
    public int placeSingleBid(double bidAmount, int userId, int auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            // Set các tham số đầu vào (IN)
            cstmt.setDouble(1, bidAmount);
            cstmt.setInt(2, userId);
            cstmt.setInt(3, auctionId);
            
            // Đăng ký tham số đầu ra (OUT) cho p_status_code
            cstmt.registerOutParameter(4, Types.INTEGER);
            
            cstmt.execute();
            
            // Trả về kết quả từ Procedure
            return cstmt.getInt(4);
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thực thi Procedure PRO_PlaceSingleBid: " + e.getMessage());
            return -2; // Mã lỗi hệ thống
        }
    }
}
