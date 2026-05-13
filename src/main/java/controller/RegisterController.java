package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {
    @FXML private TextField txtFullName, txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = txtFullName.getText();
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String role = cbRole.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (userDAO.isUsernameExists(user)) {
            showAlert("Lỗi", "Tên đăng nhập đã tồn tại!");
            return;
        }

        if (userDAO.register(user, pass, name, role)) {
            showAlert("Thành công", "Đăng ký hoàn tất! Hãy đăng nhập.");
            goToLogin(event);
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}