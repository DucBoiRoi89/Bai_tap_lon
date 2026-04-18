class Vehicle extends Items {
    public Vehicle(String id, String name, double startPrice, String description) {
        super(id, name, startPrice, description);
    }

    @Override
    public void printInfor() {
        System.out.println("Vehicle: " + name);
    }
}