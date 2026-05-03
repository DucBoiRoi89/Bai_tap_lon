package dao;
import config.DatabaseConnection;
import model.*;
import java.sql.*;
public class AuctionDAO {

    /**
     * Chức năng 3.1.3: Đặt giá qua Stored Procedure 
     *
     */
    public int placeSingleBid(double bidAmount, int userId, int auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setDouble(1, bidAmount);
            cstmt.setInt(2, userId);
            cstmt.setInt(3, auctionId);
            cstmt.registerOutParameter(4, Types.INTEGER);
            
            cstmt.execute();
            return cstmt.getInt(4); // 1: Thành công, 0: Giá thấp, -1: Đóng, -2: Lỗi
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi PRO_PlaceSingleBid: " + e.getMessage());
            return -2;
        }
    }
    public int getHighestBidderId(int auctionId) {
        String sql = "SELECT bidder_id FROM BID_TRANSACTIONS WHERE auction_id = ? " +
                     "ORDER BY bid_amount DESC, bid_time ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("bidder_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Chức năng 3.1.2: cập nhật item
     *
     */
    public boolean updateItem(Item item) {
        String sqlUpdateBase = "UPDATE ITEMS SET item_name = ?, description = ? WHERE item_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // 1. Cập nhật bảng cha
            try (PreparedStatement psBase = conn.prepareStatement(sqlUpdateBase)) {
                psBase.setString(1, item.getName());
                psBase.setString(2, "Mô tả sản phẩm");
                psBase.setInt(3, Integer.parseInt(item.getId()));
                psBase.executeUpdate();
            }

            // 2. Cập nhật bảng con tương ứng
            if (item instanceof Electronics) {
                updateElectronics(conn, (Electronics) item);
            } else if (item instanceof Art) {
                updateArt(conn, (Art) item);
            } else if (item instanceof Vehicle) {
                updateVehicle(conn, (Vehicle) item);
            }
            conn.commit(); 
            return true;
        } catch (SQLException | NumberFormatException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    private void updateElectronics(Connection conn, Electronics e) throws SQLException {
        String sql = "UPDATE ELECTRONICS SET brand = ?, warranty_months = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getBrand());
            ps.setInt(2, e.getWarrantyMonths());
            ps.setInt(3, Integer.parseInt(e.getId()));
            ps.executeUpdate();
        }
    }
    private void updateArt(Connection conn, Art a) throws SQLException {
        String sql = "UPDATE ART SET author = ?, creation_year = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getArtist());
            ps.setInt(2, a.getYearCreated());
            ps.setInt(3, Integer.parseInt(a.getId()));
            ps.executeUpdate();
        }
    }
    private void updateVehicle(Connection conn, Vehicle v) throws SQLException {
        String sql = "UPDATE VEHICLES SET brand = ?, license_plate = ?, mileage = ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getBrand());
            ps.setString(2, v.getLicensePlate());
            ps.setLong(3, v.getMileage());
            ps.setInt(4, Integer.parseInt(v.getId()));
            ps.executeUpdate();
        }
    }

    /**
     * Chức năng 3.1.2: Xóa sản phẩm an toàn
     */
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM ITEMS WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.err.println("Không thể xóa sản phẩm đang đấu giá!");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }
}
