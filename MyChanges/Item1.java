package model;

import java.time.LocalDateTime;

public class Item1 extends Item {

    public Item1(
            String id,
            String name,
            double price
    ) {

        super(
                id,
                name,
                "Test description",
                price,
                LocalDateTime.now().plusMinutes(5)
        );
    }

    @Override
    public String getCategory() {
        return "Test";
    }
}