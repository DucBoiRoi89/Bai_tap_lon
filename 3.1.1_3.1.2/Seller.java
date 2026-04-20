import java.util.List;
import java.util.Arrays;

import java.util.ArrayList;

class Seller extends User {
    private List<Items> myItems;

    public Seller(String id, String name, String password, String email, int phoneNumber) {
        super(id, name, password, email, phoneNumber);
        this.myItems = new ArrayList<>();
    }

    @Override
    public void displayRolePermissions() {
        System.out.println("Seller permissions");
    }
}