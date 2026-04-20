class Electronics extends Items {
    public Electronics(String id, String name, double startPrice, String description) {
        super(id, name, startPrice, description);
    }
    @Override
    public void printInfor() {
        System.out.println("Electronics: " + name);
    }
}