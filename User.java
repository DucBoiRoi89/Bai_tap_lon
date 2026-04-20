/**
 * Lớp User trừu tượng làm cha cho Bidder, Seller, Admin.
 * Thể hiện tính Inheritance (Kế thừa).
 */
abstract class User extends Entity {
    private String email;
    private String password;
    private String phoneNumber;

    // Giữ nguyên Constructor cũ của bạn
    public User(String id, String name, String email) {
        super(id, name);
        this.email = email;
    }

    // --- PHẦN BỔ SUNG: CÁC PHƯƠNG THỨC GETTER/SETTER ---
    // (Giúp UserManager và UserDAO hết báo lỗi đỏ)

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // --- CÁC PHƯƠNG THỨC CŨ GIỮ NGUYÊN ---

    public boolean login() {
        System.out.println(getName() + " đã đăng nhập.");
        return true;
    }

    public void logout() {
        System.out.println(getName() + " đã đăng xuất.");
    }
}