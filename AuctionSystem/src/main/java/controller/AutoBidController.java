package controller;

import dao.AuctionDAO;
import model.UserSession;
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
                new Alert(Alert.AlertType.INFORMATION, "Đã kích hoạt robot đấu giá cho bạn!").show();
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