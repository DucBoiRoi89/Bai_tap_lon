package model;
public class Vehicle extends Item {
    private String brand;
    private String licensePlate;
    private long mileage;
    public Vehicle(String id, String name, String description, String brand, String licensePlate, long mileage) {
        super(id, name, description, "VEHICLE"); // Gọi constructor của lớp cha Item
        this.brand = brand;
        this.licensePlate = licensePlate;
        this.mileage = mileage;
    }

    public String getBrand() { 
        return brand; 
    }
    
    public String getLicensePlate() { 
        return licensePlate; 
    }
    
    public long getMileage() { 
        return mileage; 
    }
    public void setBrand(String brand) { this.brand = brand; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setMileage(long mileage) { this.mileage = mileage; }
}