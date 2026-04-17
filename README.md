Trigger là thủ tục lưu trữ (stored procedure) đặc biệt được tự động thực thi khi
xảy ra một sự kiện cụ thể trên bảng, ví dụ trước hoặc sau khi thực hiện
INSERT, UPDATE, hoặc DELETE.


DROP TRIGGER IF EXISTS AntiSniping; ----> nếu bảng tên AntiSniping và thuộc loại TRIGGER tồn tại thì xoá bảng đó
DELIMITER //
CREATE TRIGGER AntiSniping -------- > tạo bảng trigger 
AFTER INSERT ON bid_transactions ------- > ngay khi một người thay dổi dữ liệu vào bảng bid_transactions
FOR EACH ROW 
BEGIN -----> bắt đầu thực hiện lệnh ở dưới
DECLARE v_auction_id INT ; --------> tạo một biến có kiểu INT . Tránh nhầm lẫn ở một số câu lệnh và tính ổn định dữ liệu vì dữ liệu gốc có biến động trong quá 
trình chạy thì dữ liệu được sao lưu vẫn chạy bình thường 

DECLARE current_end_time DATETIME; ------> tạo một biến có kiểu timestamp trung với thời gian thực trên đồng hồ khu vực

DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED');  ------ tạo một biến trạng thái để xác định trạng thái hiện tại của cuộc đấu giá
SET v_auction_id = NEW.auction_id; -----> NEW này biểu diễn việc sự thay đổi . nếu có phần dòng thay mà nằm cùng dòng auction_id thì NEW.auction id biểu diễn cho việc dòng auction thây đổi 

SELECT status INTO v_status  FROM auctions WHERE auction_id = v_auction_id;  ------> gán status từ bảng AUCTIONS vào biến v_status     WHERE  v_auction_id = NEW.auction_id có nghĩa rằng hệ thống sẽ truy vấn khắp cái bảng sao cho tìm thấy auction_id được chỉ định và lưu vào trùng với auction_id vừa bị thay đổi một phần tử nào đó nằm chung dòng với nó



SELECT end_time INTO current_end_time ----------> lấy END_TIME từ bảng AUCTION gán vào current_end_time
    FROM AUCTIONS 
    WHERE auction_id = v_auction_id; 

IF TIMESTAMPDIFF(SECOND, NOW() , current_end_time ) < 30 and v_status = 'RUNNING' THEN --------- > kiểm tra  khoảng cách thời gian thực và thời gian kết thục là dưới 30s hay không  hoặc trạng thái hiện tại có phải RUNNING 
UPDATE AUCTIONS --------> nếu vượt qua điều kiên thì lập trức update bảng autction với thông tin update ở dưới 
SET end_time = DATE_ADD(current_end_time, INTERVAL 1 MINUTE) nâng thời gian kết thúc lên 1p ở trong bảng auction tại nơi mà auction_id trùng với auction_id bị thay đổi
        WHERE auction_id = NEW.auction_id; 
    END IF;
END //
DELIMITER ;



