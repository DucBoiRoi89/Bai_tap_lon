package model;

import java.time.LocalDateTime;
import java.util.Map;

public class ItemFactory {

    /**.
     * @param type 
     * @param id Mã sản phẩm
     * @param name Tên sản phẩm
     * @param description Mô tả
     * @param startingPrice Giá khởi điểm (MỚI)
     * @param endTime Thời gian kết thúc (MỚI)
     * @param details Các thông số kỹ thuật riêng của từng loại
     * @return 
     */
    public static Item createItem(String type, String id, String name, String description, 
                                  double startingPrice, LocalDateTime endTime, Map<String, Object> details) {
        
        if (type == null) return null;

        switch (type.toUpperCase()) {
            case "ELECTRONICS":
                String brand = (String) details.getOrDefault("brand", "Unknown");
                int warranty = (int) details.getOrDefault("warrantyMonths", 0);
                return new Electronics(id, name, description, startingPrice, endTime, brand, warranty);

            case "ART":
                String artist = (String) details.getOrDefault("artist", "Unknown");
                int year = (int) details.getOrDefault("yearCreated", 0);
                return new Art(id, name, description, startingPrice, endTime, artist, year);

            case "VEHICLE":
                String vBrand = (String) details.getOrDefault("brand", "Unknown");
                String license = (String) details.getOrDefault("licensePlate", "N/A");
                long mileage = (long) details.getOrDefault("mileage", 0L);
                return new Vehicle(id, name, description, startingPrice, endTime, vBrand, license, mileage);

            default:
                return null;
        }
    }
}