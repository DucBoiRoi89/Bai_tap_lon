import model.Electronics;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    private Electronics createItem() {
        return new Electronics("I1", "Laptop", "Mô tả", 500, LocalDateTime.now().plusDays(1), "Dell", 12);
    }

    @Test
    public void testCurrentPriceInitiallyEqualsStartingPrice() {
        Electronics item = createItem();
        assertEquals(500, item.getCurrentPrice());
    }

    @Test
    public void testSetCurrentPrice() {
        Electronics item = createItem();
        item.setCurrentPrice(700);
        assertEquals(700, item.getCurrentPrice());
    }

    @Test
    public void testGetName() {
        Electronics item = createItem();
        assertEquals("Laptop", item.getName());
    }
}