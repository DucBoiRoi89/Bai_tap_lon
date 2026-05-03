
package controller;
import core.AuctionSocketClient;
import model.AuctionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;

    @FXML
    public void initialize() {
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
    }
    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            AuctionSocketClient.getInstance().sendRequest(amount);
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số hợp lệ");
        }
    }
    private void handleServerEvent(AuctionEvent event) {
        switch (event.getType()) {
            case NEW_BID:
                lblCurrentPrice.setText("Giá hiện tại: " + event.getData());
                break;
            case OUTBID:
                showWarning("Bạn đã bị vượt giá!");
                break;
            case TIME_EXTENDED:
                showInfo("Phiên đấu giá được gia hạn!");
                break;
        }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

