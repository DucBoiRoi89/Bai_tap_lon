import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.Duration;
import java.time.LocalDateTime;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {

    // Ánh xạ đến đúng cái FlowPane trong Scene Builder
    @FXML
    private FlowPane itemContainer ;

    // Gọi DAO để lấy dữ liệu
    private ItemDAO itemDao = new ItemDAO() ; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hàm này tự động chạy khi màn hình vừa mở lên
        loadItemsFromDatabase() ;
    }

    private void loadItemsFromDatabase() {
        // Lấy toàn bộ sản phẩm đang đấu giá từ Database
        List<Item> items = itemDao.getAllItems() ;
        
        // Xóa các thẻ cũ (nếu có) để tránh trùng lặp
        itemContainer.getChildren().clear() ;

        // Duyệt qua từng sản phẩm và tạo thẻ sản phẩm
        for (Item item : items) {
            VBox card = createItemCard(item) ;
            itemContainer.getChildren().add(card) ; // Nhét thẻ vào FlowPane
        }
    }

    // Hàm tạo giao diện thẻ 
    private VBox createItemCard(Item item) {
        // Tạo VBox mô phỏng lại đúng những gì bạn đã làm trên Scene Builder
        VBox card = new VBox(3) ; // Spacing = 3
        card.setPrefSize(175, 200) ;
        card.setAlignment(Pos.TOP_CENTER) ;
        
        // CSS bo góc, nền trắng, đổ bóng cực nhẹ
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                      "-fx-padding: 10;") ;

        // Tên sản phẩm
        Label lblName = new Label(item.getName()) ;
        lblName.setFont(Font.font("Serif", FontWeight.BOLD, 16)) ;

        // Giá sản phẩm
        Label lblPrice = new Label(item.getCurrentPrice() + " VNĐ") ;
        lblPrice.setStyle("-fx-background-color: #dca742; -fx-text-fill: white; " +
                          "-fx-padding: 5 15 5 15; -fx-background-radius: 3;") ;
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 14)) ;

        Label lblTime = new Label() ;
        lblTime.setFont(Font.font("System", 13)) ;
        lblTime.setStyle("-fx-text-fill: #7f8c8d;") ;

        // Nút BID NOW (KẾT NỐI VỚI MÀN HÌNH ĐÃ CODE)
        Button btnBid = new Button("BID NOW") ;
        btnBid.setPrefWidth(175);
        // btnBid.setStyle("-fx-background-color: black; -fx-text-fill: white; " +
        //                 "-fx-background-radius: 3; -fx-cursor: hand;") ;

        // Lấy thời gian hiện tại và tính toán khoảng cách
        LocalDateTime now = LocalDateTime.now() ;
        LocalDateTime endTime = item.getEndTime() ;
        
       if (endTime != null) {
        Duration duration = Duration.between(now, endTime) ;

            if (duration.isNegative() || duration.isZero()) {
                // NẾU ĐÃ HẾT GIỜ: Đổi chữ thành màu đỏ và khóa nút bấm
                lblTime.setText("Thời gian: Đã kết thúc") ;
                lblTime.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;") ;
                
                btnBid.setText("ĐÃ ĐÓNG") ;
                btnBid.setDisable(true) ; // KHÓA NÚT, không cho click vào trang đặt giá nữa
                btnBid.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-background-radius: 3;") ;
            } else {
                // NẾU CÒN GIỜ: Tính toán Giờ, Phút, Giây
                long hours = duration.toHours() ;
                long minutes = duration.toMinutes() % 60 ;
                long seconds = duration.getSeconds() % 60 ;
                
                // Format chuỗi theo chuẩn "02h 15m 30s left"
                lblTime.setText(String.format("Còn lại: %02dh %02dm %02ds", hours, minutes, seconds)) ;
                
                // Giữ nguyên style gốc của nút bấm
                btnBid.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;") ;
                btnBid.setOnAction(event -> openBiddingScreen(item)) ;
            }
        }

        // Nạp tất cả vào thẻ
        card.getChildren().addAll(lblName, lblPrice, lblTime , btnBid) ;

        return card ;
    }

    private void openBiddingScreen(Item selectedItem) {
        System.out.println("Chuẩn bị mở phòng đấu giá cho: " + selectedItem.getName()) ;
        // Code chuyển Scene sang BiddingView.fxml
    }
}