public abstract class User extends Entity {
    private String password;
    private String email;
    private int phoneNumber;

    public User(String id, String name, String password, String email, int phoneNumber) {
        super(id, name);
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public abstract void displayRolePermissions();
}