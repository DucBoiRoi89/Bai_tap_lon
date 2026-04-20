class Art extends Items {
    public Art(String id, String name, double startPrice, String description) {
        super(id, name, startPrice, description);
    }

    @Override
    public void printInfor() {
        System.out.println("Art: " + name);
    }
}