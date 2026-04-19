/**
 * Lớp Quản trị viên.
 */
class Admin extends User {
    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    // Quản lý người dùng: Có thể khóa hoặc kích hoạt
    public void manageUser(User user) {
        System.out.println("Admin đang kiểm tra người dùng: " + user.getEmail());
    }

    // Chức năng can thiệp hệ thống
    public void cancelAuction(Auction auction) {
        auction.end(); // Kết thúc cưỡng ép
        System.out.println("Admin đã hủy phiên đấu giá: " + auction.getAuctionId());
    }
}