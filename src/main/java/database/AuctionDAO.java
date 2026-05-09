package database;

import common.entities.*;
import java.sql.*; // Import các lớp thực thể (Item, Electronics, Art, Vehicle)
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    /**
     * Chức năng 3.1.3: Đặt giá qua Stored Procedure 
     * Kết hợp logic xử lý lỗi từ file 1 và kiểu dữ liệu linh hoạt
     */
    public int placeSingleBid(double bidAmount, String userId, String auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setDouble(1, bidAmount);
            cstmt.setString(2, userId);
            cstmt.setString(3, auctionId);
            cstmt.registerOutParameter(4, Types.INTEGER);
            
            cstmt.execute();
            return cstmt.getInt(4);
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi PRO_PlaceSingleBid: " + e.getMessage());
            return -2;
        }
    }

    /**
     * Lấy ID người đặt giá cao nhất hiện tại
     */
    public String getHighestBidderId(String auctionId) {
        String sql = "SELECT bidder_id FROM BID_TRANSACTIONS WHERE auction_id = ? " +
                     "ORDER BY bid_amount DESC, bid_time ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("bidder_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Chức năng 3.1.2: Cập nhật Item (Hỗ trợ đa hình: Art, Electronics, Vehicle)
     * Sử dụng Transaction (Commit/Rollback) để đảm bảo an toàn dữ liệu
     */
    public boolean updateItem(Item item) {
        String sqlUpdateBase = "UPDATE items SET item_name = ?, description = ? WHERE item_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // 1. Cập nhật bảng cha (ITEMS)
            try (PreparedStatement psBase = conn.prepareStatement(sqlUpdateBase)) {
                psBase.setString(1, item.getName());
                psBase.setString(2, item.getDescription()); 
                psBase.setString(3, item.getId());
                psBase.executeUpdate();
            }

            // 2. Cập nhật bảng con tương ứng dựa trên loại sản phẩm
            if (item instanceof Electronics) {
                updateElectronics(conn, (Electronics) item);
            } else if (item instanceof Art) {
                updateArt(conn, (Art) item);
            } else if (item instanceof Vehicle) {
                updateVehicle(conn, (Vehicle) item);
            }

            conn.commit(); // Hoàn tất giao dịch
            return true;
        } catch (SQLException | NumberFormatException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            // Không nên đóng connection singleton ở đây vì sẽ làm hỏng các luồng khác
            try { if(conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    private void updateElectronics(Connection conn, Electronics e) throws SQLException {
        String sql = "UPDATE items SET brand = ?, warranty_months = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getBrand());
            ps.setInt(2, e.getWarrantyMonths());
            ps.setString(3, e.getId());
            ps.executeUpdate();
        }
    }

    private void updateArt(Connection conn, Art a) throws SQLException {
        String sql = "UPDATE items SET artist = ?, year_created = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getArtist());
            ps.setInt(2, a.getYearCreated());
            ps.setString(3, a.getId());
            ps.executeUpdate();
        }
    }

    private void updateVehicle(Connection conn, Vehicle v) throws SQLException {
        String sql = "UPDATE items SET license_plate = ?, mileage = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getLicensePlate());
            ps.setLong(2, v.getMileage());
            ps.setString(3, v.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Chức năng 3.1.2: Xóa sản phẩm an toàn (Xử lý lỗi ràng buộc FK)
     */
    public boolean deleteItem(String itemId) {
        String sql = "DELETE FROM ITEMS WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.err.println("Không thể xóa sản phẩm đang trong phiên đấu giá!");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Lấy danh sách ID các phiên đang RUNNING nhưng đã quá hạn
     */
    public List<Integer> getExpiredAuctions() {
        List<Integer> expiredAuctions = new ArrayList<>();
        String sql = "SELECT auction_id FROM AUCTIONS WHERE status = 'RUNNING' AND end_time <= NOW()";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                expiredAuctions.add(rs.getInt("auction_id"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi quét phiên đấu giá hết hạn: " + e.getMessage());
        }
        return expiredAuctions;
    }

    /**
     * Cập nhật trạng thái của phiên đấu giá (Ví dụ: Chuyển từ RUNNING sang FINISHED)
     */
    public void updateAuctionStatus(int auctionId, String status) {
        String sql = "UPDATE AUCTIONS SET status = ? WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, auctionId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái phiên: " + e.getMessage());
        }
    }

    /**
     * Hàm đóng kết nối an toàn để tránh rò rỉ tài nguyên
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}