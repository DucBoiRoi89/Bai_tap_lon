import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Người đấu giá.
 */
class Bidder extends User {
    public Bidder(String id, String name, String email) {
        super(id, name, email);
    }

    // Chức năng 3.1.3: Tham gia đấu giá
    public void placeBid(Item item, double amount) {
        BidTransaction bid = new BidTransaction(this.getId(), item.getId(), amount);
        AuctionManager.getInstance().processBid(bid);
    }

    // Chức năng 3.2.1: Đấu giá tự động (Nâng cao)
    public void setAutoBid(Item item, double maxBid) {
        System.out.println("Đã thiết lập tự động đấu giá cho " + item.getName() + " tới mức: " + maxBid);
    }
}