

import model.Item1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testCurrentPriceInitiallyEqualsStartingPrice() {

        Item1 item =
                new Item1(
                        "I1",
                        "Laptop",
                        500
                );

        assertEquals(
                500,
                item.getCurrentPrice()
        );
    }

    @Test
    public void testSetCurrentPrice() {

        Item1 item =
                new Item1(
                        "I1",
                        "Laptop",
                        500
                );

        item.setCurrentPrice(700);

        assertEquals(
                700,
                item.getCurrentPrice()
        );
    }

    @Test
    public void testGetName() {

        Item1 item =
                new Item1(
                        "I1",
                        "Laptop",
                        500
                );

        assertEquals(
                "Laptop",
                item.getName()
        );
    }
}