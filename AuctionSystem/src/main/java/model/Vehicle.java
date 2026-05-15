package model;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String brand;
    private String licensePlate;
    private long mileage;

    public Vehicle(String id, String name, String description, double startingPrice, 
                   LocalDateTime endTime, String brand, String licensePlate, long mileage) {
        super(id, name, description, startingPrice, endTime);
        this.brand = brand;
        this.licensePlate = licensePlate;
        this.mileage = mileage;
    }

    public String getBrand() { return brand; }
    public String getLicensePlate() { return licensePlate; }
    public long getMileage() { return mileage; }
    @Override
public String getCategory() { return "VEHICLE"; }
}