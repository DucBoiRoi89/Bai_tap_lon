package model;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String id, String name, String description, double startingPrice, 
                       LocalDateTime endTime, String brand, int warrantyMonths) {
        super(id, name, description, startingPrice, endTime);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
    @Override
public String getCategory() { return "ELECTRONICS"; }
}