import java.util.List;
import java.util.ArrayList;
class Auction {
    private String auctionId;
    private Item item;
    private Seller seller;
    private BidTransaction highestBid;
    private List<BidTransaction> history = new ArrayList<>();

    public Auction(String id, Item item, Seller seller) {
        this.auctionId = id;
        this.item = item;
        this.seller = seller;
    }

    public void start() {
        item.setStatus(AuctionStatus.RUNNING);
    }

    public void end() {
        item.setStatus(AuctionStatus.FINISHED);
    }

    // Thêm lượt đấu giá vào lịch sử của phiên
    public void addBid(BidTransaction bid) {
        this.history.add(bid);
        this.highestBid = bid;
        this.item.updatePrice(bid.getAmount());
    }

    public String getAuctionId() { return auctionId; }
    public Item getItem() { return item; }
}