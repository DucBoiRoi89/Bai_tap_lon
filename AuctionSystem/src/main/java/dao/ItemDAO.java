package dao;

import config.DatabaseConnection;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDAO {
    
    // Tự động đồng bộ trạng thái: Chuyển RUNNING -> FINISHED nếu đã qua thời gian kết thúc
    public void syncExpiredAuctions() {
        String sql = "UPDATE AUCTIONS SET status = 'FINISHED' WHERE status = 'RUNNING' AND end_time <= NOW()";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Hàm 1: Dùng cho trang chủ (Chỉ hiển thị sản phẩm đang RUNNING)
    public List<Item> getAllItems() {
        syncExpiredAuctions(); // Gọi đồng bộ trước khi load dữ liệu
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, auc.auction_id, auc.end_time, auc.starting_price, " +
                 "COALESCE(auc.current_max_price, auc.starting_price) AS current_price, " +
                 "e.brand AS e_brand, e.warranty_months, a.author, a.creation_year, " +
                 "v.brand AS v_brand, v.license_plate, v.mileage " +
                 "FROM ITEMS i " +
                 "INNER JOIN AUCTIONS auc ON i.item_id = auc.item_id " + 
                 "LEFT JOIN ELECTRONICS e ON i.item_id = e.item_id " +
                 "LEFT JOIN ART a ON i.item_id = a.item_id " +
                 "LEFT JOIN VEHICLES v ON i.item_id = v.item_id " +
                 "WHERE auc.status IN ('RUNNING', 'FINISHED') " + // Lấy cả hàng đang chạy và đã kết thúc
                 "ORDER BY CASE WHEN auc.status = 'RUNNING' THEN 1 ELSE 2 END, auc.end_time ASC"; // Ưu tiên hàng đang chạy lên đầu trang
                 
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Item item = extractItemFromResultSet(rs);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    // Hàm 2: DÀNH RIÊNG CHO SELLER (Lấy mọi trạng thái, nhưng chỉ của 1 Seller)
    public List<Item> getSellerItems(int sellerId) {
        syncExpiredAuctions(); // Gọi đồng bộ trước khi load dữ liệu
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, auc.auction_id, auc.end_time, auc.starting_price, " +
                 "COALESCE(auc.current_max_price, auc.starting_price) AS current_price, " +
                 "e.brand AS e_brand, e.warranty_months, a.author, a.creation_year, " +
                 "v.brand AS v_brand, v.license_plate, v.mileage " +
                 "FROM ITEMS i " +
                 "INNER JOIN AUCTIONS auc ON i.item_id = auc.item_id " + 
                 "LEFT JOIN ELECTRONICS e ON i.item_id = e.item_id " +
                 "LEFT JOIN ART a ON i.item_id = a.item_id " +
                 "LEFT JOIN VEHICLES v ON i.item_id = v.item_id " +
                 "WHERE i.seller_id = ?"; // <-- Lọc theo ID người bán
                 
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item item = extractItemFromResultSet(rs);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    // Hàm phụ trợ để tránh lặp code dài dòng
    private Item extractItemFromResultSet(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        Map<String, Object> details = new HashMap<>();
        
        if ("ELECTRONICS".equalsIgnoreCase(category)) {
            details.put("brand", rs.getString("e_brand"));
            details.put("warrantyMonths", rs.getInt("warranty_months"));
        } else if ("ART".equalsIgnoreCase(category)) {
            details.put("artist", rs.getString("author"));
            details.put("yearCreated", rs.getInt("creation_year"));
        } else if ("VEHICLE".equalsIgnoreCase(category)) {
            details.put("brand", rs.getString("v_brand"));
            details.put("licensePlate", rs.getString("license_plate"));
            details.put("mileage", rs.getLong("mileage"));
        }

        Item item = ItemFactory.createItem(category, String.valueOf(rs.getInt("item_id")), 
                    rs.getString("item_name"), rs.getString("description"), 
                    rs.getDouble("starting_price"), rs.getTimestamp("end_time").toLocalDateTime(), details);
        
        if (item != null) {
            item.setCurrentPrice(rs.getDouble("current_price"));
            item.setAuctionId(rs.getInt("auction_id"));
        }
        return item;
    }
}