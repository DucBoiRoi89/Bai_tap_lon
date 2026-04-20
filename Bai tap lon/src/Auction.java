import java.util.ArrayList;
import java.util.List;

public class Auction {
    private double currentPrice;
    private User currentWinner;
    private long endTime;
    private String status;

    private List<BidTransaction> bids;

    // Constructor
    public Auction(long durationMillis) {
        this.currentPrice = 0;
        this.currentWinner = null;
        this.endTime = System.currentTimeMillis() + durationMillis;
        this.status = "RUNNING";
        this.bids = new ArrayList<>();}
    public synchronized boolean placeBid(User user, double amount) {
        long now = System.currentTimeMillis();
        if (now >= endTime) {
            closeAuction();
            return false;
        }
        if (amount <= currentPrice) {
            return false;
        }
        if (!status.equals("RUNNING")) {
            return false;
        }
        currentPrice = amount;
        currentWinner = user;

        bids.add(new BidTransaction(user, amount));
        return true;
    }
    public void checkAndCloseAuction(){
        long now = System.currentTimeMillis();
        if (now>=endTime && status.equals("RUNNING"){
            closeAuction();

        }
    }
    private void closeAuction() {
        status = "FINISHED";

        System.out.println("===== ĐẤU GIÁ KẾT THÚC =====");

        if (currentWinner != null) {
            System.out.println("Người thắng: " + currentWinner);
            System.out.println("Giá thắng: " + currentPrice);
        } else {
            System.out.println("Không có ai tham gia");
        }
    }
    public void showHistory() {
        for (BidTransaction bid : bids) {
            System.out.println(bid);
        }
    }
}

}




