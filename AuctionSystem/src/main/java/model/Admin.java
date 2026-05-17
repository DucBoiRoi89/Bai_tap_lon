package model;

public class Admin extends User {
    public Admin(int userId, String username, String password) {
        super(userId, username, password, "ADMIN");
    }

    @Override
public String getDashboardType() {
    return "AdminDashboard.fxml"; 
}
}