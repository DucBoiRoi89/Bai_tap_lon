package controller;

import dao.UserDAO;
import dao.ItemDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import java.io.IOException;

public class AdminDashboardController {
    @FXML private Label lblTotalUsers, lblActiveAuctions, lblTotalRevenue;
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername, colFullName, colRole, colStatus;
    @FXML private TableColumn<User, Double> colBalance;

    private UserDAO userDAO = new UserDAO();
    private ItemDAO itemDAO = new ItemDAO();

    @FXML
    public void initialize() {
        // Cấu hình các cột cho bảng người dùng
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("email")); // Trong model User của bạn, email đang được dùng lưu thông tin phụ
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        loadStatistics();
        tableUsers.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));
    }

    private void loadStatistics() {
        // Giả lập số liệu cho demo
        lblTotalUsers.setText("156");
        lblActiveAuctions.setText("12");
        lblTotalRevenue.setText("25,400,000 VNĐ");
    }

    @FXML private void manageUsers(ActionEvent e) { /* Chuyển tab hoặc lọc bảng */ }
    
    @FXML private void manageAuctions(ActionEvent e) {
        try {
            // Admin có thể xem chung giao diện với Buyer nhưng có quyền xóa sản phẩm
            Parent root = FXMLLoader.load(getClass().getResource("/views/BiddingView.fxml"));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void showStatistics(ActionEvent e) { loadStatistics(); }

    @FXML 
    private void handleLockUser(ActionEvent e) {
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new Alert(Alert.AlertType.CONFIRMATION, "Khóa tài khoản " + selected.getUsername() + "?").show();
        }
    }

    @FXML 
    private void handleUnlockUser(ActionEvent e) {
        // Logic mở khóa tài khoản
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}