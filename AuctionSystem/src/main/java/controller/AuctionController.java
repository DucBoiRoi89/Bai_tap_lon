package controller;
import com.google.gson.Gson;
import core.AuctionSocketClient;
import model.AuctionEvent;
import model.ClientRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;
    private Gson gson = new Gson();
    @FXML
    public void initialize() {
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
        watchAuction(1);
    }
    private void watchAuction(int auctionId) {
        ClientRequest req = new ClientRequest("WATCH", auctionId, 0, 0.0);
        AuctionSocketClient.getInstance().sendRequest(gson.toJson(req));
    }

    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            int currentAuctionId = 1; 
            int currentUserId = 2;
            ClientRequest req = new ClientRequest("BID", currentAuctionId, currentUserId, amount);
            String jsonRequest = gson.toJson(req);
            AuctionSocketClient.getInstance().sendRequest(jsonRequest);
            
            System.out.println("Đã gửi yêu cầu đặt giá: " + jsonRequest);
            
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số tiền hợp lệ!");
        }
    }
    private void handleServerEvent(AuctionEvent event) {
        switch (event.getType()) {
            case NEW_BID:
                lblCurrentPrice.setText("Giá hiện tại: " + event.getData());
                break;
            case OUTBID:
                showWarning(event.getData().toString());
                break;
            case TIME_EXTENDED:
                showInfo(event.getData().toString());
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