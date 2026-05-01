import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BiddingController {
    @FXML
    private Label lblItemName;       
    @FXML
    private Label lblCurrentPrice;  
    @FXML
    private TextField txtBidAmount ;
    private AuctionService auctionService = new AuctionService() ; 
    private User currentUser ; 
    private Item currentItem ;

    // Hàm khởi tạo dữ liệu khi màn hình vừa mở lên
    public void initData(Item item, User user) {
        this.currentItem = item ;
        this.currentUser = user ;
        
        lblItemName.setText("Đang đấu giá: " + item.getName()) ;
        lblCurrentPrice.setText(item.getCurrentPrice() + " VNĐ") ;
    }

    @FXML
    public void handlePlaceBidAction(ActionEvent event) {
        // Kiểm tra xem có rỗng không 
        String inputStr = txtBidAmount.getText() ;
        if (inputStr == null || inputStr.trim().isEmpty()) {
            AlertHelper.showWarning("Lỗi nhập liệu", "Vui lòng nhập số tiền bạn muốn đặt!") ;
            return ;
        }

        try {
            //  Ép kiểu dữ liệu (Sẽ ném NumberFormatException nếu nhập chữ)
            double amountInput = Double.parseDouble(inputStr) ;

            // Tạo đối tượng Bid và gọi xuống tầng Service
            BidTransaction newBid = new BidTransaction(currentUser.getId(), currentItem.getId(), amountInput) ;
            
            boolean isSuccess = auctionService.processBid(newBid) ;

            // Nếu đặt giá thành công , cập nhật lại label trên màn hình 
            if (isSuccess) {
                AlertHelper.showSuccess("Thành công", "Bạn đã đặt giá thành công!");
                lblCurrentPrice.setText(txtBidAmount.getText() + " VNĐ") ; // Cập nhật UI
                txtBidAmount.clear() ; // xoá trắng ô nhập liệu 
            }

        } catch (NumberFormatException e) {
            // Người dùng nhập sai định dạng số 
            AlertHelper.showWarning("Lỗi nhập liệu", "Số tiền không hợp lệ. Vui lòng chỉ nhập số!") ;

        } catch (InvalidBidException e) {
            // Giá đặt thấp hơn giá hiện tại 
            AlertHelper.showWarning("Từ chối đặt giá", e.getMessage()) ;

        } catch (AuctionClosedException e) {
            // Đấu giá khi phiên đã đóng 
            AlertHelper.showWarning("Phiên đấu giá kết thúc", e.getMessage()) ;

        } catch (DatabaseException e) {
            // Lỗi dữ liệu/SQL 
            System.err.println("Database Error: " + e.getMessage()) ;
            e.getCause().printStackTrace() ; 
            AlertHelper.showError("Lỗi kết nối dữ liệu", "Hệ thống đang gặp sự cố. Vui lòng thử lại sau!") ;

        } catch (AuctionBaseException e) {
            // Bắt các lỗi nghiệp vụ khác 
            AlertHelper.showWarning("Lỗi đấu giá", e.getMessage()) ;
            
        } catch (Exception e) {
            e.printStackTrace() ;
            AlertHelper.showError("Lỗi không xác định", "Đã xảy ra lỗi hệ thống: " + e.getMessage()) ;
        }
    }
}
