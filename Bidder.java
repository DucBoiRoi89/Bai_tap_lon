public class Bidder extends User {
    public Bidder(String id, String username, String password, String email, int phoneNumber) {
        super(id, username, password, email, phoneNumber);
    }

    @Override
    public void displayRolePermissions() {
        System.out.println("Quyền: Xem sản phẩm, Đặt giá (Bid).");
    }
}