package controller;

import dao.AuctionDAO;
import dao.ItemDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SellerDashboardController {
    @FXML private TableView<Item> tableItems;
    @FXML private TableColumn<Item, String> colId, colName, colDesc;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private TableColumn<Item, LocalDateTime> colEndTime;

    @FXML private VBox dynamicFieldsContainer; 
    @FXML private TextField txtItemName, txtStartingPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbCategory;
    @FXML private DatePicker dpEndTime;
    @FXML private Label lblDialogTitle;
    @FXML private Button btnSave;

    private TextField txtBrand, txtWarranty, txtAuthor, txtYear, txtLicense, txtMileage;
    private AuctionDAO auctionDAO = new AuctionDAO();
    private ItemDAO itemDAO = new ItemDAO();

    // Khai báo biến cờ để phân biệt Thêm hay Sửa
    private boolean isEditMode = false;
    private int editingItemId = -1;

    @FXML 
    public void initialize() {
        if (cbCategory != null) {
            cbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        }
        if (tableItems != null) {
            colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startingPrice"));
            colDesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
            colEndTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endTime"));
            
            colEndTime.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            });
            loadTableData();
        }
    }

    private void loadTableData() {
        // Lấy ID của người bán đang đăng nhập
        int sellerId = UserSession.getLoggedInUser().getUserId();
        
        // Gọi hàm getSellerItems thay vì getAllItems
        tableItems.setItems(FXCollections.observableArrayList(itemDAO.getSellerItems(sellerId)));
    }
    @FXML
    private void handleCategoryChange() {
        dynamicFieldsContainer.getChildren().clear();
        String selected = cbCategory.getValue();
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        if ("ELECTRONICS".equals(selected)) {
            txtBrand = new TextField(); txtWarranty = new TextField();
            grid.addRow(0, new Label("Thương hiệu:"), txtBrand);
            grid.addRow(1, new Label("Bảo hành (tháng):"), txtWarranty);
        } else if ("ART".equals(selected)) {
            txtAuthor = new TextField(); txtYear = new TextField();
            grid.addRow(0, new Label("Tác giả:"), txtAuthor);
            grid.addRow(1, new Label("Năm sáng tác:"), txtYear);
        } else if ("VEHICLE".equals(selected)) {
            txtBrand = new TextField(); txtLicense = new TextField(); txtMileage = new TextField();
            grid.addRow(0, new Label("Hãng xe:"), txtBrand);
            grid.addRow(1, new Label("Biển số:"), txtLicense);
            grid.addRow(2, new Label("Số KM:"), txtMileage);
        }
        dynamicFieldsContainer.getChildren().add(grid);
    }

    @FXML 
    private void handleSaveProduct(ActionEvent event) {
        try {
            // Lọc bỏ dấu phẩy hoặc khoảng trắng nếu người dùng copy/paste tiền tệ
            String rawPrice = txtStartingPrice.getText().replaceAll("[,\\.\\s]", "");
            double price = Double.parseDouble(rawPrice);
            
            LocalDateTime end;
            if (dpEndTime.getValue().equals(java.time.LocalDate.now())) {
                // MẸO DEMO: Nếu chọn ngày hôm nay, tự động set thời gian kết thúc là 2 phút tính từ bây giờ
                end = LocalDateTime.now().plusMinutes(2);
            } else {
                end = dpEndTime.getValue().atTime(23, 59, 59);
            }
            String cat = cbCategory.getValue();

            Map<String, Object> details = new HashMap<>();
            if ("ELECTRONICS".equals(cat)) {
                details.put("brand", txtBrand.getText());
                // Lọc bỏ chữ cái lỡ nhập vào ô số
                details.put("warrantyMonths", Integer.parseInt(txtWarranty.getText().replaceAll("\\D", "")));
            } else if ("ART".equals(cat)) {
                details.put("artist", txtAuthor.getText());
                details.put("yearCreated", Integer.parseInt(txtYear.getText().replaceAll("\\D", "")));
            } else if ("VEHICLE".equals(cat)) {
                details.put("brand", txtBrand.getText());
                details.put("licensePlate", txtLicense.getText());
                details.put("mileage", Long.parseLong(txtMileage.getText().replaceAll("\\D", "")));
            }

            // Dùng ID thật nếu đang Sửa, dùng "0" nếu Thêm mới
            String itemIdStr = isEditMode ? String.valueOf(editingItemId) : "0";
            Item item = ItemFactory.createItem(cat, itemIdStr, txtItemName.getText(), 
                        txtDescription.getText(), price, end, details);
            
            int sellerId = UserSession.getLoggedInUser().getUserId();
            boolean success;

            if (isEditMode) {
                success = auctionDAO.updateItem(item, sellerId);
            } else {
                success = auctionDAO.insertItem(item, sellerId);
            }

            if (success) {
                ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Lưu thất bại! Hệ thống không thể xử lý yêu cầu.").show();
            }
        } catch (NumberFormatException e) { 
            // Bắt đúng lỗi parse số
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập số hợp lệ vào các ô Giá/Bảo hành/Năm/KM!").show();
        } catch (Exception e) {
            // Hiển thị lỗi thật ra console để dễ debug nếu có lỗi khác
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi hệ thống: " + e.getMessage()).show();
        }
    }
    @FXML
    private void handleStartAuction(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null && auctionDAO.startAuction(Integer.parseInt(selected.getId()))) {
            new Alert(Alert.AlertType.INFORMATION, "Phiên đấu giá đã bắt đầu!").show();
            loadTableData();
        }
    }

    @FXML 
    private void handleDeleteProduct(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Sản phẩm này cùng toàn bộ lịch sử đấu giá sẽ bị xóa vĩnh viễn. Bạn có chắc chắn không?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            
            if (confirm.getResult() == ButtonType.YES) {
                int sellerId = UserSession.getLoggedInUser().getUserId();
                if (auctionDAO.deleteItem(Integer.parseInt(selected.getId()), sellerId)) {
                    loadTableData();
                    new Alert(Alert.AlertType.INFORMATION, "Đã xóa sản phẩm thành công!").show();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Không thể xóa!\n\nSản phẩm đang trong phiên đấu giá (RUNNING). Vui lòng đợi phiên đấu giá kết thúc để xóa.").show();
                }
            }
        }
    }

    @FXML private void handleGoToHome(ActionEvent e) { switchScene(e, "BiddingView.fxml"); }
    @FXML private void handleGoToProfile(ActionEvent e) { switchScene(e, "AccountProfile.fxml"); }
    @FXML private void handleLoadMyProducts(ActionEvent e) { loadTableData(); }
    
    @FXML 
    private void handleShowAddDialog(ActionEvent e) throws IOException { 
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddItemDialog.fxml"));
        Parent root = loader.load();
        
        SellerDashboardController dialogCtrl = loader.getController();
        dialogCtrl.isEditMode = false; // Đặt lại cờ là Thêm mới

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
        loadTableData();
    }

    @FXML 
    private void handleShowUpdateDialog(ActionEvent e) throws IOException {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn sản phẩm cần sửa!").show();
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddItemDialog.fxml"));
        Parent root = loader.load();
        
        SellerDashboardController dialogCtrl = loader.getController();
        dialogCtrl.setEditingData(selected); 
        
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
        loadTableData(); 
    }

   public void setEditingData(Item item) {
        this.isEditMode = true; // Bật cờ Sửa
        this.editingItemId = Integer.parseInt(item.getId());

        lblDialogTitle.setText("SỬA THÔNG TIN SẢN PHẨM");
        btnSave.setText("CẬP NHẬT");
        txtItemName.setText(item.getName());
        
        // Hiển thị giá không có đuôi thập phân .0
        txtStartingPrice.setText(String.format("%.0f", item.getStartingPrice()));
        txtDescription.setText(item.getDescription());
        dpEndTime.setValue(item.getEndTime().toLocalDate());
        
        // Gán Category và KHÓA lại
        cbCategory.setValue(item.getCategory());
        cbCategory.setDisable(true); 

        // QUAN TRỌNG: Gọi hàm tạo giao diện động ngay lập tức
        handleCategoryChange();

        // Điền dữ liệu cũ vào các ô động vừa được tạo ra
        if (item instanceof Electronics e) {
            txtBrand.setText(e.getBrand());
            txtWarranty.setText(String.valueOf(e.getWarrantyMonths()));
        } else if (item instanceof Art a) {
            txtAuthor.setText(a.getArtist());
            txtYear.setText(String.valueOf(a.getYearCreated()));
        } else if (item instanceof Vehicle v) {
            txtBrand.setText(v.getBrand());
            txtLicense.setText(v.getLicensePlate());
            txtMileage.setText(String.valueOf(v.getMileage()));
        }
    }

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/" + fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    @FXML private void handleCancel(ActionEvent e) { ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); }
}