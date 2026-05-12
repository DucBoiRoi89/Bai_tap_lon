package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        
        User loggedInUser = userDAO.login(username, password);

        if (loggedInUser != null) {
            UserSession.setLoggedInUser(loggedInUser);
            
            try {
                String dashboardPath = "/views/" + loggedInUser.getDashboardType();
                Parent root = FXMLLoader.load(getClass().getResource(dashboardPath));
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("UET Auctions - " + loggedInUser.getRole());
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Lỗi hệ thống", "Không thể nạp giao diện: " + loggedInUser.getDashboardType());
            }
        } else {
            showError("Đăng nhập thất bại", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
    }
    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Register.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng ký tài khoản - UET Auctions");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("LỖI: Không tìm thấy file Register.fxml tại /views/");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}