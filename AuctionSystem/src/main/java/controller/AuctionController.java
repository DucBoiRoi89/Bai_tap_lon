package controller;

import core.AuctionSocketClient;
import model.AuctionEvent;
import model.ClientRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;
    private int currentAuctionId = 1; 
    private int loggedInUserId = 2; 

    @FXML
    public void initialize() {
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
        watchAuction(currentAuctionId);
    }

    private void watchAuction(int auctionId) {
        ClientRequest req = new ClientRequest("WATCH", auctionId, loggedInUserId, 0.0);
        AuctionSocketClient.getInstance().sendRequest(req);
    }

    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            
            // Gửi request đặt giá lên Server
            ClientRequest req = new ClientRequest("BID", currentAuctionId, loggedInUserId, amount);
            AuctionSocketClient.getInstance().sendRequest(req);
        
            // Xóa trắng ô nhập sau khi gửi thành công
            txtBidAmount.clear(); 
            
        } catch (NumberFormatException e) {
            // Báo lỗi ngay lập tức nếu người dùng nhập chữ hoặc bỏ trống
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    private void handleServerEvent(AuctionEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case NEW_BID:
                    lblCurrentPrice.setText("Giá hiện tại: " + event.getData().toString());
                    break;
                case OUTBID:
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", event.getData().toString());
                    break;
                    
                case TIME_EXTENDED:
                    showAlert(Alert.AlertType.INFORMATION, "Gia hạn thời gian", event.getData().toString());
                    break;
                    
                case ERROR:
                    showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", event.getData().toString());
                    break;
                    
                case AUCTION_FINISHED:
                    showAlert(Alert.AlertType.INFORMATION, "Kết thúc", event.getData().toString());
                    break;
            }
        });
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void setAuctionData(int auctionId, double startPrice) {
    this.currentAuctionId = auctionId;
    this.lblCurrentPrice.setText("Giá hiện tại: " + startPrice + " VNĐ");
    watchAuction(currentAuctionId);
}
}