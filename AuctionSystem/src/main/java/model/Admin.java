package model;

public class Admin extends User {
    public Admin(int userId, String username, String password, String email) {
        super(userId, username, password, email, "ADMIN");
    }

    @Override
public String getDashboardType() {
    return "AdminDashboard.fxml"; 
}
}