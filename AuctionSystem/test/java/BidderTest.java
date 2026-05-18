import model.Bidder;
// import model.BidTransaction;
import model.Electronics;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BidderTest {

    // Helper tạo Bidder nhanh
    private Bidder createBidder() {
        return new Bidder(1, "Nam", "pass123", 1000.0);
    }

    // Helper tạo Item dùng Electronics (class con có sẵn của Item)
    private Electronics createItem(double startingPrice) {
        return new Electronics(
                "I1", "Laptop", "Laptop test",
                startingPrice,
                LocalDateTime.now().plusDays(1),
                "Dell", 12
        );
    }

    // =========================================================
    // 1. TEST KHỞI TẠO BIDDER
    // =========================================================

    @Test
    public void testCreateBidder() {
        Bidder bidder = createBidder();
        assertEquals("Nam", bidder.getUsername());
        assertEquals(1, bidder.getUserId());
    }

    @Test
    public void testBidderRole() {
        Bidder bidder = createBidder();
        assertEquals("BIDDER", bidder.getRole());
    }

    @Test
    public void testBidderBalance() {
        Bidder bidder = createBidder();
        assertEquals(1000.0, bidder.getBalance());
    }

    @Test
    public void testBidderSetBalance() {
        Bidder bidder = createBidder();
        bidder.setBalance(2000.0);
        assertEquals(2000.0, bidder.getBalance());
    }

    @Test
    public void testBidderDashboardType() {
        Bidder bidder = createBidder();
        assertEquals("BiddingView.fxml", bidder.getDashboardType());
    }


}
