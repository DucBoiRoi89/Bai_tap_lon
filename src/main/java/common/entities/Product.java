package common.entities;

public abstract class Product {
    protected int id;
    protected String name;
    // BẮT BUỘC THÊM: ID của người bán
    protected String sellerId; 

    public Product(int id, String name, String sellerId) {
        this.id = id;
        this.name = name;
        this.sellerId = sellerId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    // BẮT BUỘC THÊM: Getter
    public String getSellerId() { return sellerId; }
}