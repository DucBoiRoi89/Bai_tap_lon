package dao;

import config.DatabaseConnection;
import model.Item;
import model.ItemFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDAO {

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();

        String sql = "SELECT i.*, auc.end_time, COALESCE(auc.current_max_price, auc.starting_price) AS current_price, " +
                     "e.brand AS e_brand, e.warranty_months, " +
                     "a.author, a.creation_year, " +
                     "v.brand AS v_brand, v.license_plate, v.mileage " +
                     "FROM ITEMS i " +
                     "INNER JOIN AUCTIONS auc ON i.item_id = auc.item_id " +
                     "LEFT JOIN ELECTRONICS e ON i.item_id = e.item_id " +
                     "LEFT JOIN ART a ON i.item_id = a.item_id " +
                     "LEFT JOIN VEHICLES v ON i.item_id = v.item_id " +
                     "WHERE auc.status IN ('OPEN', 'RUNNING')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("item_id"));
                String name = rs.getString("item_name");
                String desc = rs.getString("description");
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
                
                Item item = ItemFactory.createItem(category, id, name, desc, details);
            
                if (item != null) {
                    Timestamp endTimestamp = rs.getTimestamp("end_time");
                    if (endTimestamp != null) {
                        item.setEndTime(endTimestamp.toLocalDateTime());
                    }
                    item.setCurrentPrice(rs.getDouble("current_price"));
                    
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách sản phẩm: " + e.getMessage());
        }
        return items;
    }
}