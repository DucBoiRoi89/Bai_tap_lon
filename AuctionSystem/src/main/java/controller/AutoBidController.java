package controller;

import dao.AuctionDAO;
import model.UserSession;
import service.AuctionService;
import service.AutoBidService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class AutoBidController {
    @FXML private TextField txtMaxBid, txtIncrement;
    private int currentAuctionId;
    private AuctionDAO auctionDAO = new AuctionDAO();

    public void setAuctionId(int auctionId) { this.currentAuctionId = auctionId; }

    @FXML
    private void handleActivate() {
        try {
            // FIX LỖI: Xóa bỏ các dấu phẩy, dấu chấm, khoảng trắng do người dùng nhập vào
            String rawMaxBid = txtMaxBid.getText().replaceAll("[,\\.\\s]", "");
            String rawIncrement = txtIncrement.getText().replaceAll("[,\\.\\s]", "");
            
            double maxBid = Double.parseDouble(rawMaxBid);
            double increment = Double.parseDouble(rawIncrement);
            int userId = UserSession.getLoggedInUser().getUserId();

            if (auctionDAO.saveAutoBid(currentAuctionId, userId, maxBid, increment)) {
                // FIX: Kích hoạt bot kiểm tra và nhảy giá ngay lập tức nếu bạn chưa nắm giữ TOP 1
                AuctionService auctionService = new AuctionService();
                double currentPrice = auctionDAO.getCurrentMaxPrice(currentAuctionId);
                int highestBidderId = auctionDAO.getHighestBidderId(currentAuctionId);
                
                new AutoBidService().triggerAutoBids(currentAuctionId, currentPrice, highestBidderId, auctionService);
                
                // Cập nhật thông báo thông minh để tránh hiểu lầm cho người dùng
                if (highestBidderId == userId) {
                    new Alert(Alert.AlertType.INFORMATION, "Đã kích hoạt Robot!\n\nTuy nhiên, bạn đang giữ Top 1 nên Robot sẽ đi 'ngủ' để tránh tự đè giá chính mình. Hãy yên tâm, nó sẽ tự thức dậy nhảy số khi có ai đó vượt giá bạn!").show();
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Đã kích hoạt Robot!\n\nRobot đang bắt đầu phân tích và giành lại Top 1 cho bạn!").show();
                }
                
                ((Stage) txtMaxBid.getScene().getWindow()).close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Lỗi hệ thống, không thể lưu cấu hình Robot!").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng chỉ nhập số, không nhập chữ!").show();
        }
    }

    @FXML private void handleCancel() { ((Stage) txtMaxBid.getScene().getWindow()).close(); }
}