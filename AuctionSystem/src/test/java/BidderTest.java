import Bidder;
import BidTransaction;
import model.Electronics;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BidderTest {

    // Helper tạo Bidder nhanh
    private Bidder createBidder() {
        return new Bidder(1, "Nam", "pass123", "nam@email.com", 1000.0);
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

    // =========================================================
    // 2. TEST placeBid() - ĐẶT GIÁ HỢP LỆ
    // =========================================================

    @Test
    public void testBidderPlaceValidBid() {
        // Giá hiện tại = 500, đặt 700 → hợp lệ
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction bid = new BidTransaction(
                String.valueOf(bidder.getUserId()),
                item.getId(),
                700
        );

        boolean result = bidder.placeBid(item, bid);

        assertTrue(result);
    }

    @Test
    public void testPlaceBidUpdatesItemCurrentPrice() {
        // Sau khi đặt thành công, currentPrice phải cập nhật
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction bid = new BidTransaction("1", item.getId(), 800);

        bidder.placeBid(item, bid);

        assertEquals(800, item.getCurrentPrice());
    }

    @Test
    public void testPlaceBidJustOneAboveCurrent() {
        // Hơn đúng 1 đồng → hợp lệ
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction bid = new BidTransaction("1", item.getId(), 501);

        assertTrue(bidder.placeBid(item, bid));
    }

    // =========================================================
    // 3. TEST placeBid() - ĐẶT GIÁ KHÔNG HỢP LỆ
    // =========================================================

    @Test
    public void testBidderPlaceLowerBidFails() {
        // Giá hiện tại = 500, đặt 400 → thất bại
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction lowBid = new BidTransaction("1", item.getId(), 400);

        boolean result = bidder.placeBid(item, lowBid);

        assertFalse(result);
    }

    @Test
    public void testPlaceBidEqualCurrentPriceFails() {
        // Đặt đúng bằng giá hiện tại → thất bại
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction equalBid = new BidTransaction("1", item.getId(), 500);

        assertFalse(bidder.placeBid(item, equalBid));
    }

    @Test
    public void testPlaceBidZeroAmountFails() {
        Electronics item = createItem(500);
        Bidder bidder = createBidder();
        BidTransaction zeroBid = new BidTransaction("1", item.getId(), 0);

        assertFalse(bidder.placeBid(item, zeroBid));
    }

    @Test
    public void testPlaceBidNullTransactionFails() {
        Electronics item = createItem(500);
        Bidder bidder = createBidder();

        assertFalse(bidder.placeBid(item, null));
    }

    @Test
    public void testPlaceBidNullItemFails() {
        Bidder bidder = createBidder();
        BidTransaction bid = new BidTransaction("1", "I1", 700);

        assertFalse(bidder.placeBid(null, bid));
    }

    // =========================================================
    // 4. TEST ĐẶT GIÁ NHIỀU LẦN
    // =========================================================

    @Test
    public void testMultipleBidsIncreasingOrder() {
        Electronics item = createItem(500);
        Bidder bidder = createBidder();

        assertTrue(bidder.placeBid(item, new BidTransaction("1", "I1", 600)));
        assertTrue(bidder.placeBid(item, new BidTransaction("1", "I1", 700)));
        assertTrue(bidder.placeBid(item, new BidTransaction("1", "I1", 1000)));
        assertEquals(1000, item.getCurrentPrice());
    }

    @Test
    public void testSecondBidLowerThanFirstFails() {
        Electronics item = createItem(500);
        Bidder bidder = createBidder();

        bidder.placeBid(item, new BidTransaction("1", "I1", 700));
        boolean result = bidder.placeBid(item, new BidTransaction("1", "I1", 650));

        assertFalse(result);
        assertEquals(700, item.getCurrentPrice());
    }
}