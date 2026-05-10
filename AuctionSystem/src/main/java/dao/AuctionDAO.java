package dao;

import config.DatabaseConnection;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    // =========================================================================
    // 1. QUẢN LÝ SẢN PHẨM (CRUD) - DÀNH CHO SELLER
    // =========================================================================
    
   public boolean insertItem(Item item, int sellerId) {
    // 1. Tách SQL cho 2 bảng đúng theo bang.sql
    String sqlItems = "INSERT INTO ITEMS (item_name, description, seller_id, category) VALUES (?, ?, ?, ?)";
    String sqlAuctions = "INSERT INTO AUCTIONS (item_id, start_time, end_time, starting_price, current_max_price, status) VALUES (?, NOW(), ?, ?, ?, 'OPEN')";
    
    Connection conn = null;
    try {
        conn = DatabaseConnection.getInstance().getConnection();
        conn.setAutoCommit(false); // Quan trọng cho Data Engineer: Đảm bảo tính Transaction

        int newItemId = -1;
        // BƯỚC 1: Chèn vào ITEMS
        try (PreparedStatement psItems = conn.prepareStatement(sqlItems, Statement.RETURN_GENERATED_KEYS)) {
            psItems.setString(1, item.getName());
            psItems.setString(2, item.getDescription());
            psItems.setInt(3, sellerId);
            psItems.setString(4, getItemCategory(item));
            psItems.executeUpdate();

            try (ResultSet rs = psItems.getGeneratedKeys()) {
                if (rs.next()) newItemId = rs.getInt(1);
            }
        }

        if (newItemId == -1) throw new SQLException("Lỗi: Không tạo được ID sản phẩm.");

        // BƯỚC 2: Chèn vào AUCTIONS
        try (PreparedStatement psAuc = conn.prepareStatement(sqlAuctions)) {
            psAuc.setInt(1, newItemId);
            psAuc.setTimestamp(2, Timestamp.valueOf(item.getEndTime()));
            psAuc.setDouble(3, item.getStartingPrice());
            psAuc.setDouble(4, item.getStartingPrice()); // Giá cao nhất hiện tại ban đầu = giá khởi điểm
            psAuc.executeUpdate();
        }

        // BƯỚC 3: Chèn bảng con (Electronics/Art/Vehicle)
        insertSubCategory(conn, newItemId, item);

        conn.commit(); 
        return true;
    } catch (SQLException e) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
        e.printStackTrace();
        return false;
    } finally {
        closeConnection(conn);
    }
}
public void updateAuctionStatus(int itemId, String status) {
    String sql = "UPDATE AUCTIONS SET status = ? WHERE item_id = ?"; 
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, status);
        ps.setInt(2, itemId);
        ps.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
}
   public boolean updateItem(Item item, int sellerId) {
    // 1. Cập nhật thông tin cơ bản ở bảng ITEMS
    String sqlUpdateItems = "UPDATE ITEMS SET item_name = ?, description = ? WHERE item_id = ? AND seller_id = ?";
    // 2. Cập nhật thông tin đấu giá ở bảng AUCTIONS
    String sqlUpdateAuctions = "UPDATE AUCTIONS SET starting_price = ?, end_time = ? WHERE item_id = ?";
    
    Connection conn = null;
    try {
        conn = DatabaseConnection.getInstance().getConnection();
        conn.setAutoCommit(false); // Bắt đầu Transaction

        int itemId = Integer.parseInt(item.getId());

        // Cập nhật ITEMS
        try (PreparedStatement psItems = conn.prepareStatement(sqlUpdateItems)) {
            psItems.setString(1, item.getName());
            psItems.setString(2, item.getDescription());
            psItems.setInt(3, itemId);
            psItems.setInt(4, sellerId);
            if (psItems.executeUpdate() == 0) return false; 
        }

        // Cập nhật AUCTIONS
        try (PreparedStatement psAuc = conn.prepareStatement(sqlUpdateAuctions)) {
            psAuc.setDouble(1, item.getStartingPrice());
            psAuc.setTimestamp(2, Timestamp.valueOf(item.getEndTime()));
            psAuc.setInt(3, itemId);
            psAuc.executeUpdate();
        }

        conn.commit();
        return true;
    } catch (SQLException e) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
        e.printStackTrace();
        return false;
    } finally { closeConnection(conn); }
}

    public boolean deleteItem(int itemId, int sellerId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Kiểm tra trạng thái trước
            String sqlCheck = "SELECT status FROM AUCTIONS WHERE item_id = ?";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, itemId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && (rs.getString("status").equals("RUNNING") || rs.getString("status").equals("OPEN"))) {
                // Nếu muốn cho xóa hàng trong kho (OPEN) thì bỏ chữ OPEN ở trên
                if(rs.getString("status").equals("RUNNING")) return false; 
            }

            // Thực hiện xóa theo các bước ( CASCADE ngầm )
            String sqlDel = "DELETE FROM ITEMS WHERE item_id = ? AND seller_id = ?";
            PreparedStatement psDel = conn.prepareStatement(sqlDel);
            psDel.setInt(1, itemId);
            psDel.setInt(2, sellerId);
            return psDel.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    // =========================================================================
    // 2. CHỨC NĂNG ĐẤU GIÁ
    // =========================================================================

    public int placeSingleBid(double bidAmount, int userId, int auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setDouble(1, bidAmount);
            cstmt.setInt(2, userId);
            cstmt.setInt(3, auctionId);
            cstmt.registerOutParameter(4, Types.INTEGER);
            cstmt.execute();
            return cstmt.getInt(4);
        } catch (SQLException e) { return -2; }
    }

    public int getHighestBidderId(int itemId) {
        String sql = "SELECT bidder_id FROM BID_TRANSACTIONS WHERE item_id = ? ORDER BY bid_amount DESC, bid_time ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("bidder_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<Integer> getExpiredAuctions() {
        String sql = "SELECT item_id FROM AUCTIONS WHERE status = 'OPEN' AND end_time <= NOW()";
        List<Integer> expired = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) expired.add(rs.getInt("item_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return expired;
    }

    // =========================================================================
    // 3. CÁC HÀM TRỢ GIÚP CHI TIẾT (ĐÃ ĐỊNH NGHĨA ĐẦY ĐỦ)
    // =========================================================================

    private String getItemCategory(Item item) {
        if (item instanceof Electronics) return "ELECTRONICS";
        if (item instanceof Art) return "ART";
        if (item instanceof Vehicle) return "VEHICLE";
        return "UNKNOWN";
    }

    // --- Hỗ trợ ELECTRONICS ---
    private void insertElectronics(Connection conn, int id, Electronics e) throws SQLException {
        String sql = "INSERT INTO ELECTRONICS (item_id, brand, warranty_months) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, e.getBrand());
            ps.setInt(3, e.getWarrantyMonths());
            ps.executeUpdate();
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

    // --- Hỗ trợ ART ---
    private void insertArt(Connection conn, int id, Art a) throws SQLException {
        String sql = "INSERT INTO ART (item_id, author, creation_year) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, a.getArtist());
            ps.setInt(3, a.getYearCreated());
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

    // --- Hỗ trợ VEHICLE ---
    private void insertVehicle(Connection conn, int id, Vehicle v) throws SQLException {
        String sql = "INSERT INTO VEHICLES (item_id, brand, license_plate, mileage) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getLicensePlate());
            ps.setLong(4, v.getMileage());
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
   public boolean startAuction(int itemId) {
        String sql = "UPDATE AUCTIONS SET status = 'RUNNING', start_time = NOW() WHERE item_id = ? AND status = 'OPEN'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    private void insertSubCategory(Connection conn, int id, Item item) throws SQLException {
    if (item instanceof Electronics) {
        insertElectronics(conn, id, (Electronics) item);
    } else if (item instanceof Art) {
        insertArt(conn, id, (Art) item);
    } else if (item instanceof Vehicle) {
        insertVehicle(conn, id, (Vehicle) item);
    }
}

}