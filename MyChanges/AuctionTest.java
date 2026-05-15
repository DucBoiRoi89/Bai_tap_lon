import model.*;
import exception.InvalidBidException;
import exception.AuctionClosedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {

    // Helper tạo nhanh
    private Bidder createBidder(int id, String name, double balance) {
        return new Bidder(id, name, "pass", "email@test.com", balance);
    }

    private Item1 createItem(String id, String name, double price) {
        return new Item1(id, name, price);
    }

    // =========================================================
    // 1. TEST MODEL - AuctionEvent
    // =========================================================

    @Test
    public void testAuctionEventCreation() {
        AuctionEvent event = new AuctionEvent(AuctionEvent.Type.NEW_BID, 1, 500.0);

        assertEquals(AuctionEvent.Type.NEW_BID, event.getType());
        assertEquals(1, event.getItemId());
        assertEquals(500.0, event.getData());
    }

    @Test
    public void testAuctionEventTypes() {
        // Kiểm tra tất cả các loại event tồn tại
        assertNotNull(AuctionEvent.Type.NEW_BID);
        assertNotNull(AuctionEvent.Type.OUTBID);
        assertNotNull(AuctionEvent.Type.TIME_EXTENDED);
        assertNotNull(AuctionEvent.Type.AUCTION_FINISHED);
        assertNotNull(AuctionEvent.Type.ERROR);
    }

    @Test
    public void testAuctionEventWithStringData() {
        AuctionEvent event = new AuctionEvent(
                AuctionEvent.Type.OUTBID, 2, "Bạn đã bị vượt giá!"
        );

        assertEquals("Bạn đã bị vượt giá!", event.getData());
    }

    // =========================================================
    // 2. TEST MODEL - Item (dùng Item1 là class con cụ thể)
    // =========================================================

    @Test
    public void testItemCreation() {
        Item1 item = createItem("I1", "Laptop", 500.0);

        assertEquals("I1", item.getId());
        assertEquals("Laptop", item.getName());
        assertEquals(500.0, item.getStartingPrice());
    }

    @Test
    public void testItemCurrentPriceEqualsStartingPriceInitially() {
        // Khi mới tạo, currentPrice phải bằng startingPrice
        Item1 item = createItem("I1", "Laptop", 500.0);

        assertEquals(item.getStartingPrice(), item.getCurrentPrice());
    }

    @Test
    public void testItemSetCurrentPrice() {
        Item1 item = createItem("I1", "Laptop", 500.0);
        item.setCurrentPrice(750.0);

        assertEquals(750.0, item.getCurrentPrice());
    }

    @Test
    public void testItemSetAuctionId() {
        Item1 item = createItem("I1", "Laptop", 500.0);
        item.setAuctionId(101);

        assertEquals(101, item.getAuctionId());
    }

    @Test
    public void testItemEndTimeIsInFuture() {
        Item1 item = createItem("I1", "Laptop", 500.0);

        assertTrue(item.getEndTime().isAfter(LocalDateTime.now()));
    }

    @Test
    public void testItemCategory() {
        Item1 item = createItem("I1", "Laptop", 500.0);

        assertEquals("Test", item.getCategory());
    }

    // =========================================================
    // 3. TEST MODEL - Seller
    // =========================================================

    @Test
    public void testSellerCreation() {
        Seller seller = new Seller(10, "TuanSeller", "pass", "tuan@email.com", 5000.0);

        assertEquals("TuanSeller", seller.getUsername());
        assertEquals("SELLER", seller.getRole()); // phải là SELLER, không phải BIDDER
        assertEquals(5000.0, seller.getBalance());
    }

    @Test
    public void testSellerDashboardType() {
        Seller seller = new Seller(10, "TuanSeller", "pass", "tuan@email.com", 5000.0);

        assertEquals("MyProductsView.fxml", seller.getDashboardType());
    }

    @Test
    public void testSellerInheritsPlaceBid() {
        // Seller kế thừa Bidder nên cũng có thể đặt giá
        Seller seller = new Seller(10, "TuanSeller", "pass", "tuan@email.com", 5000.0);
        Item1 item = createItem("I2", "Phone", 300.0);
        BidTransaction bid = new BidTransaction("10", "I2", 500.0);

        assertTrue(seller.placeBid(item, bid));
    }

    // =========================================================
    // 4. TEST MODEL - BidRecord
    // =========================================================

    @Test
    public void testBidRecordCreation() {
        BidRecord record = new BidRecord("Laptop", 700.0, LocalDateTime.now());

        assertEquals("Laptop", record.getItemName());
        assertEquals(700.0, record.getBidAmount());
    }

    @Test
    public void testBidRecordFormattedPrice() {
        BidRecord record = new BidRecord("Laptop", 1000000.0, LocalDateTime.now());

        // getFormattedPrice() trả về dạng "1,000,000 VNĐ"
        assertTrue(record.getFormattedPrice().contains("VNĐ"));
    }

    @Test
    public void testBidRecordFormattedTime() {
        BidRecord record = new BidRecord("Laptop", 700.0, LocalDateTime.of(2025, 1, 15, 10, 30, 0));

        assertEquals("15/01/2025 10:30:00", record.getFormattedTime());
    }

    // =========================================================
    // 5. TEST EXCEPTION
    // =========================================================

    @Test
    public void testInvalidBidExceptionMessage() {
        InvalidBidException ex = new InvalidBidException("Giá không hợp lệ");

        assertEquals("Giá không hợp lệ", ex.getMessage());
    }

    @Test
    public void testAuctionClosedExceptionMessage() {
        AuctionClosedException ex = new AuctionClosedException("Phiên đã đóng");

        assertEquals("Phiên đã đóng", ex.getMessage());
    }

    // =========================================================
    // 6. TEST AutoBidConfig
    // =========================================================

    @Test
    public void testAutoBidConfigCreation() {
        AutoBidConfig config = new AutoBidConfig(1, 101, 5, 2000.0, 100.0, null);

        assertEquals(101, config.getAuctionId());
        assertEquals(5, config.getUserId());
        assertEquals(2000.0, config.getMaxBidAmount());
        assertEquals(100.0, config.getIncrement());
    }

    @Test
    public void testAutoBidConfigCompareToWithBothNull() {
        // Cả 2 createdAt đều null → bằng nhau (compareTo = 0)
        AutoBidConfig c1 = new AutoBidConfig(1, 101, 5, 2000.0, 100.0, null);
        AutoBidConfig c2 = new AutoBidConfig(2, 101, 6, 1500.0, 50.0, null);

        assertEquals(0, c1.compareTo(c2));
    }

    @Test
    public void testAutoBidConfigCompareToNullIsLast() {
        // config có createdAt = null sẽ đứng sau config có createdAt thực
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        AutoBidConfig withTime = new AutoBidConfig(1, 101, 5, 2000.0, 100.0, now);
        AutoBidConfig withNull = new AutoBidConfig(2, 101, 6, 1500.0, 50.0, null);

        // withNull.compareTo(withTime) > 0 → null đứng sau
        assertTrue(withNull.compareTo(withTime) > 0);
        // withTime.compareTo(withNull) < 0 → có thời gian đứng trước
        assertTrue(withTime.compareTo(withNull) < 0);
    }
}