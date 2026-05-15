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
public void updateAuctionStatus(int auctionId, String status) {
        String sql = "UPDATE AUCTIONS SET status = ? WHERE auction_id = ?"; 
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, auctionId);
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
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            
            // 1. Kiểm tra trạng thái, thời gian và lấy auction_id
            String sqlCheck = "SELECT auction_id, status, end_time FROM AUCTIONS WHERE item_id = ?";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, itemId);
            ResultSet rs = psCheck.executeQuery();
            
            int auctionId = -1;
            if (rs.next()) {
                String status = rs.getString("status");
                Timestamp endTime = rs.getTimestamp("end_time");
                boolean isTimeUp = endTime != null && endTime.getTime() <= System.currentTimeMillis();
                
                // Chỉ chặn xóa khi phiên đấu giá ĐANG diễn ra thật sự (thời gian chưa kết thúc)
                if ("RUNNING".equalsIgnoreCase(status) && !isTimeUp) {
                    return false; // Không cho xóa khi đang chạy
                }
                auctionId = rs.getInt("auction_id");
            }

            // 2. Bật Transaction để xóa thủ công theo thứ tự: Con -> Cha
            conn.setAutoCommit(false);
            
            if (auctionId != -1) {
                // Xóa lịch sử giao dịch và cấu hình bot
                try (PreparedStatement psBid = conn.prepareStatement("DELETE FROM BID_TRANSACTIONS WHERE auction_id = ?")) {
                    psBid.setInt(1, auctionId); psBid.executeUpdate();
                }
                try (PreparedStatement psAuto = conn.prepareStatement("DELETE FROM AUTO_BID_CONFIGS WHERE auction_id = ?")) {
                    psAuto.setInt(1, auctionId); psAuto.executeUpdate();
                }
                // Xóa phiên đấu giá
                try (PreparedStatement psAuc = conn.prepareStatement("DELETE FROM AUCTIONS WHERE auction_id = ?")) {
                    psAuc.setInt(1, auctionId); psAuc.executeUpdate();
                }
            }
            
            // Xóa thông tin chi tiết các danh mục
            try (PreparedStatement psE = conn.prepareStatement("DELETE FROM ELECTRONICS WHERE item_id = ?")) { psE.setInt(1, itemId); psE.executeUpdate(); }
            try (PreparedStatement psA = conn.prepareStatement("DELETE FROM ART WHERE item_id = ?")) { psA.setInt(1, itemId); psA.executeUpdate(); }
            try (PreparedStatement psV = conn.prepareStatement("DELETE FROM VEHICLES WHERE item_id = ?")) { psV.setInt(1, itemId); psV.executeUpdate(); }
            
            // Cuối cùng xóa thông tin sản phẩm chính
            String sqlDel = "DELETE FROM ITEMS WHERE item_id = ? AND seller_id = ?";
            try (PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
                psDel.setInt(1, itemId);
                psDel.setInt(2, sellerId);
                
                if (psDel.executeUpdate() > 0) { conn.commit(); return true; } 
                else { conn.rollback(); return false; }
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
        }
    }
    // =========================================================================
    // 2. CHỨC NĂNG ĐẤU GIÁ
    // =========================================================================

   public int placeSingleBid(double amount, int userId, int auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setDouble(1, amount);
            cs.setInt(2, userId);
            cs.setInt(3, auctionId);
            cs.registerOutParameter(4, Types.INTEGER);
            
            cs.execute();
            return cs.getInt(4);
            
        } catch (SQLException e) {
            System.err.println("Lỗi gọi Procedure: " + e.getMessage());
            return -2;
        }
    }
    public int getHighestBidderId(int auctionId) {
        String sql = "SELECT bidder_id FROM BID_TRANSACTIONS WHERE auction_id = ? ORDER BY bid_amount DESC, bid_time ASC LIMIT 1";
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("bidder_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public double getCurrentMaxPrice(int auctionId) {
        String sql = "SELECT current_max_price, starting_price FROM AUCTIONS WHERE auction_id = ?";
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double current = rs.getDouble("current_max_price");
                    if (!rs.wasNull()) return current;
                    return rs.getDouble("starting_price");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public List<Integer> getExpiredAuctions() {
        String sql = "SELECT auction_id FROM AUCTIONS WHERE status = 'RUNNING' AND end_time <= NOW()";
        List<Integer> expired = new ArrayList<>();
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) expired.add(rs.getInt("auction_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return expired;
    }
    public boolean saveAutoBid(int auctionId, int userId, double maxBid, double increment) {
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection()) {
            // FIX: Xóa cấu hình cũ của user trong phiên này để tránh tạo ra nhiều Bot clone tự đè giá nhau
            String delSql = "DELETE FROM AUTO_BID_CONFIGS WHERE auction_id = ? AND user_id = ?";
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) {
                psDel.setInt(1, auctionId);
                psDel.setInt(2, userId);
                psDel.executeUpdate();
            }
            
            String sql = "INSERT INTO AUTO_BID_CONFIGS (auction_id, user_id, max_bid_amount, bid_increment, is_active) VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, auctionId);
                ps.setInt(2, userId);
                ps.setDouble(3, maxBid);
                ps.setDouble(4, increment);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
   public boolean forceCloseAuction(int auctionId) {
        String sql = "UPDATE AUCTIONS SET end_time = NOW() WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            return ps.executeUpdate() > 0;
            // Khi end_time = NOW(), AuctionMonitorService (quét mỗi 5 giây) 
            // sẽ tự động nhặt nó, chốt người thắng và broadcast kết quả!
        } catch (SQLException e) { return false; }
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
public List<BidRecord> getBidHistoryByUser(int userId) {
        List<BidRecord> history = new ArrayList<>();
        String sql = "SELECT i.item_name, b.bid_amount, b.bid_time " +
                     "FROM BID_TRANSACTIONS b " +
                     "JOIN AUCTIONS a ON b.auction_id = a.auction_id " +
                     "JOIN ITEMS i ON a.item_id = i.item_id " +
                     "WHERE b.bidder_id = ? ORDER BY b.bid_time DESC";
                     
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new BidRecord(
                        rs.getString("item_name"), 
                        rs.getDouble("bid_amount"), 
                        rs.getTimestamp("bid_time").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
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
    public double getUserBalance(int userId) {
        String sql = "SELECT balance FROM USERS WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
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