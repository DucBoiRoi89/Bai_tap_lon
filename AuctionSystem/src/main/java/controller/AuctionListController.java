package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Item;
import dao.ItemDAO;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {

    @FXML
    private FlowPane itemContainer;

    private ItemDAO itemDao = new ItemDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (itemContainer != null) {
            loadItemsFromDatabase();
        }
    }
    private void loadItemsFromDatabase() {
        List<Item> items = itemDao.getAllItems();
        itemContainer.getChildren().clear();

        for (Item item : items) {
            VBox card = createItemCard(item);
            itemContainer.getChildren().add(card);
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setPrefSize(180, 220);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 15;");

        Label lblName = new Label(item.getName());
        lblName.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label lblPrice = new Label(String.format("%,.0f VNĐ", (double)item.getCurrentPrice()));
        lblPrice.setStyle("-fx-text-fill: #dca742; -fx-font-weight: bold;");

        Label lblTime = new Label();
        lblTime.setFont(Font.font("System", 12));
        
        Button btnBid = new Button("BID NOW");
        btnBid.setMaxWidth(Double.MAX_VALUE);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = item.getEndTime();
        
        if (endTime != null) {
            Duration duration = Duration.between(now, endTime);
            if (duration.isNegative() || duration.isZero()) {
                lblTime.setText("Đã kết thúc");
                lblTime.setStyle("-fx-text-fill: #e74c3c;");
                btnBid.setDisable(true);
                btnBid.setText("CLOSED");
            } else {
                lblTime.setText(String.format("%02dh %02dm left", duration.toHours(), duration.toMinutes() % 60));
                btnBid.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-cursor: hand;");
                btnBid.setOnAction(e -> openBiddingScreen(item));
            }
        }

        card.getChildren().addAll(lblName, lblPrice, lblTime, btnBid);
        return card;
    }
    private void switchScene(javafx.event.ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Lỗi chuyển màn hình: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToHome(javafx.event.ActionEvent event) {
        switchScene(event, "BiddingView.fxml");
    }

    @FXML
    private void handleGoToProfile(javafx.event.ActionEvent event) {
        switchScene(event, "AccountProfile.fxml");
    }

    @FXML
    private void handleGoToHistory(javafx.event.ActionEvent event) {
        switchScene(event, "BidHistory.fxml");
    }
private void openBiddingScreen(Item selectedItem) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AuctionDetailView.fxml"));
        Parent root = loader.load();
        AuctionController controller = loader.getController();
        controller.setAuctionData(Integer.parseInt(selectedItem.getId()), 0.0);

        Stage stage = (Stage) itemContainer.getScene().getWindow();
        stage.getScene().setRoot(root);
    } catch (IOException e) {
        System.err.println("Lỗi khi mở màn hình chi tiết đấu giá: " + e.getMessage());
        e.printStackTrace();
    }
}
@FXML
private void handleChangePassword(javafx.event.ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChangePasswordDialog.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Đổi mật khẩu - UET Auctions");
        stage.setScene(new Scene(root));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.show();
        
    } catch (IOException e) {
        System.err.println("Không thể mở cửa sổ đổi mật khẩu!");
        e.printStackTrace();
    }
}
@FXML
private void handleCloseDialog(javafx.event.ActionEvent event) {
    Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    stage.close();
}

}