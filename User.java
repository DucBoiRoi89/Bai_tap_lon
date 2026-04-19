/**
 * Lớp User trừu tượng làm cha cho Bidder, Seller, Admin.
 * Thể hiện tính Inheritance.
 */
abstract class User extends Entity {
    private String email;
    private String password;
    private String phoneNumber;

    public User(String id, String name, String email) {
        super(id, name);
        this.email = email;
    }

    public boolean login() {
        System.out.println(getName() + " đã đăng nhập.");
        return true;
    }

    public void logout() {
        System.out.println(getName() + " đã đăng xuất.");
    }
    
    public String getEmail() { return email; }
}