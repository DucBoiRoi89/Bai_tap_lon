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
    
    // Giả lập trạng thái session người dùng (Thay vì hardcode cứng vào các hàm)
    private int currentAuctionId = 1; 
    private int loggedInUserId = 2; 

    @FXML
    public void initialize() {
        // Lắng nghe sự kiện từ Socket Server
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
        
        // Gửi yêu cầu theo dõi phiên đấu giá hiện tại khi vừa mở form
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
                    // Cập nhật giá mới lên màn hình
                    lblCurrentPrice.setText("Giá hiện tại: " + event.getData().toString());
                    break;
                    
                case OUTBID:
                    // Thông báo bị người khác vượt giá
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", event.getData().toString());
                    break;
                    
                case TIME_EXTENDED:
                    // Thông báo Anti-sniping 
                    showAlert(Alert.AlertType.INFORMATION, "Gia hạn thời gian", event.getData().toString());
                    break;
                    
                case ERROR:
                    // Bắt các ngoại lệ Custom Exception từ Server 
                    showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", event.getData().toString());
                    break;
                    
                case AUCTION_FINISHED:
                    // Thông báo kết thúc phiên
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
}