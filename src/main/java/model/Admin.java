package model;

public class Admin extends User {
    private String accessLevel; 

    public Admin(int userId, String username, String password, String email, String accessLevel) {
        super(userId, username, password, email, "ADMIN");
        this.accessLevel = accessLevel;
    }

    @Override
public String getDashboardType() {
    return "AdminDashboard.fxml"; 
}

    public String getAccessLevel() { return accessLevel; }
}