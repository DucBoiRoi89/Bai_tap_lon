package model;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths; 

    public Electronics(String id, String name, String description, String brand, int warrantyMonths) {
        super(id, name, description, "ELECTRONICS"); 
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}