
/**
 * Lớp Người đấu giá.
 */
class Bidder extends User {
    public Bidder(String id, String name, String email) {
        super(id, name, email);
    }

    // Chức năng 3.1.3: Tham gia đấu giá
    // Trong file Bidder.java
    public void placeBid(Item item, double amount) {
        BidTransaction bid = new BidTransaction(this.getId(), item.getId(), amount);
        
        // Sửa dòng này: Gọi ItemManager để xử lý logic đặt giá & check concurrency
        boolean success = ItemManager.getInstance().placeBid(item.getId(), bid);
        
        if (success) {
            System.out.println("Bidder " + getName() + " đã đặt giá thành công: " + amount);
        } else {
            System.out.println("Đặt giá thất bại (Giá thấp hơn hiện tại hoặc hết giờ).");
        }
    }

    // Chức năng 3.2.1: Đấu giá tự động (Nâng cao)
    public void setAutoBid(Item item, double maxBid) {
        System.out.println("Đã thiết lập tự động đấu giá cho " + item.getName() + " tới mức: " + maxBid);
    }
}