import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ItemFactory {
    // BẮT BUỘC phải có từ khóa static để ItemDAO có thể gọi mà không cần dùng lệnh 'new'
    public static Item createItem(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        
        // Trích xuất các trường chung
        String id = rs.getString("item_id");
        String name = rs.getString("item_name");
        String desc = rs.getString("description");
        double startPrice = rs.getDouble("start_price");
        
        Timestamp ts = rs.getTimestamp("end_time");
        LocalDateTime end = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        // Quyết định tạo loại đối tượng nào dựa trên category
        if ("Vehicle".equalsIgnoreCase(category)) {
            return new Vehicle(id, name, desc, startPrice, end, 
                               rs.getString("license_plate"), rs.getLong("mileage"));
        } else if ("Electronics".equalsIgnoreCase(category)) {
            return new Electronics(id, name, desc, startPrice, end, 
                                   rs.getString("brand"), rs.getInt("warranty_months"));
        } else if ("Art".equalsIgnoreCase(category)) {
            return new Art(id, name, desc, startPrice, end, 
                           rs.getString("artist"), rs.getInt("year_created"));
        }
        
        return null; 
    }
}