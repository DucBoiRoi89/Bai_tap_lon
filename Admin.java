class Admin extends User {
    public Admin(String id, String name, String password, String email, int phoneNumber) {
        super(id, name, password, email, phoneNumber);
    }

    @Override
    public void displayRolePermissions() {
        System.out.println("Admin permissions");
    }
}