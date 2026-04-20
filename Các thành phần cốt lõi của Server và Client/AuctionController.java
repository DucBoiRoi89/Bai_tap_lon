// File: AuctionController.java
package com.uet.auction.controller;

import com.uet.auction.core.AuctionSocketClient;
import com.uet.auction.model.AuctionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;

    @FXML
    public void initialize() {
        // Đăng ký nhận sự kiện realtime từ Server
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
    }

    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            // Gửi yêu cầu đặt giá lên Server
            AuctionSocketClient.getInstance().sendRequest(amount);
        } catch (NumberFormatException e) {
            // Xử lý lỗi ngoại lệ
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
    // ... (các code ở trên giữ nguyên)

    // Hàm hỗ trợ hiển thị thông báo lỗi
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hàm hỗ trợ hiển thị cảnh báo (Ví dụ: Bị vượt giá)
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hàm hỗ trợ hiển thị thông tin (Ví dụ: Gia hạn phiên)
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
}
