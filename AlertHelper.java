import javafx.scene.control.Alert ;

public class AlertHelper {
    public static void showSuccess(String title , String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION) ;
        alert.setTitle(title) ;
        alert.setHeaderText(null) ;
        alert.setContentText(content) ;
        alert.showAndWait() ;
    }

    public static void showWarning(String title , String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING) ;
        alert.setTitle(title) ;
        alert.setHeaderText("Cảnh báo") ;
        alert.setContentText(content) ;
        alert.showAndWait() ;
    }

    public static void showError(String title , String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR) ;
        alert.setTitle(title) ;
        alert.setHeaderText("Lỗi hệ thống") ;
        alert.setContentText(content) ;
        alert.showAndWait() ;
    }
}