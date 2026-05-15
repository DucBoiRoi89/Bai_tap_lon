package model;

public class Admin extends User {
    public Admin(int userId, String username, String password, String fullName) {
        super(userId, username, password, fullName, "ADMIN");
    }

    @Override
public String getDashboardType() {
    return "AdminDashboard.fxml"; 
}
}