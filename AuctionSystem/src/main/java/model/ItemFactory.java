package model;
import java.util.Map;
public class ItemFactory {
    public static Item createItem(String type, String id, String name, String description, Map<String, Object> details) {
        if (type == null) return null;
        switch (type.toUpperCase()) {
            case "ELECTRONICS":
                return new Electronics(
                    id, 
                    name, 
                    description, 
                    (String) details.get("brand"), 
                    ((Number) details.get("warrantyMonths")).intValue()
                );
            case "ART":
                return new Art(
                    id, 
                    name, 
                    description, 
                    (String) details.get("artist"), 
                    ((Number) details.get("yearCreated")).intValue()
                );
            case "VEHICLE":
                return new Vehicle(
                    id, 
                    name, 
                    description, 
                    (String) details.get("brand"), 
                    (String) details.get("licensePlate"),
                    ((Number) details.get("mileage")).longValue()
                );
            default:
                return null;
        }
    }
}