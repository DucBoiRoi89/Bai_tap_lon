public abstract class Items extends Entity {
    protected double startPrice;
    protected double finalPrice;
    protected String description;

    public Items(String id, String name, double startPrice, String description) {
        super(id, name);
        this.startPrice = startPrice;
        this.finalPrice = startPrice;
        this.description = description;
    }
    public abstract void printInfor();
}