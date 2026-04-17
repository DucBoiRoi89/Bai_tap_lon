
                                                        GIA HẠN 
                      


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


                                      


                                                         LOGIC AUTO BIT + BIT CON
Tích hợp đấu giá tự động với xử lý đồng thời




DELIMITER //

DROP PROCEDURE IF EXISTS PRO_bitcon //

CREATE PROCEDURE PRO_bitcon (IN p_bid_amount DECIMAL(15,2), IN p_user_id INT, IN p_auction_id INT)   --------------> tạo và truyền tham số
BEGIN 
    DECLARE v_current_price DECIMAL(15,2); 
    DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED'); 
    DECLARE v_auto_user_id INT;
    DECLARE v_auto_max DECIMAL(15,2);
    DECLARE v_auto_step DECIMAL(15,2);
    DECLARE v_next_price DECIMAL(15,2);


-------------------> tạo biến để lưu giá trị 

    -- Handler để tự động Rollback nếu có lỗi hệ thống xảy ra
    DECLARE EXIT HANDLER FOR SQLEXCEPTION  HANDLE là Catch trong java
    BEGIN -----------> tương tự try trong java
        ROLLBACK;               
    END;

---------------> cơ chế là nếu có lỗi vấn đề về UPDATE Hoặc insert thì giữ nguyên dữ liệu cũ


    START TRANSACTION;

    -----------------> Khóa dòng để tránh tranh chấp giá 
    SELECT current_max_price, status 
    INTO v_current_price, v_status
    FROM AUCTIONS
    WHERE auction_id = p_auction_id FOR UPDATE;
-------------------> gán dữ liệu vào biến đã khơi tạo ở trên




    -- Kiểm tra điều kiện đặt giá nếu hệ cuộc đấu giá vẫn còn hoạt oộng và túi tiền của mình hơn giá của vật hiện tại 
    IF v_status = 'RUNNING' AND p_bid_amount > v_current_price THEN
        
        ----->    . Lưu lượt đặt giá của người dùng để vẽ biểu đồ 
        INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount)
        VALUES (p_auction_id, p_user_id, p_bid_amount);

        UPDATE AUCTIONS 
        SET current_max_price = p_bid_amount 
        WHERE auction_id = p_auction_id; ----------------> đặt cái giá mà người dùng đã nhập vào giá hiện tại của vật tại nơi mà auction id mà người dùng muốn đặt giá trùng với auction id xuất hiện trong bảng AUCTION 
                                           
        
        ------------->  Kiểm tra Auto-bid (Chỉ lấy 1 người cài đặt cao nhất và không phải chính mình)
        SET v_auto_user_id = NULL; ---------------------> cái v_auto_user_id này sẽ nhận lần lượt id của nhiều người theo luồng chạy nếu không reset nó về null thì người sau có khi sẽ lấy luôn cái user_id của người trước 
        
        SELECT user_id, max_bid_amount, bid_increment
        INTO v_auto_user_id, v_auto_max, v_auto_step
        FROM AUTO_BID_CONFIGS
        WHERE auction_id = p_auction_id
          AND is_active = TRUE  -------------> điều kiện để gán là cuộc đấu giá còn hoạt động 
          AND user_id != p_user_id ----------------> Nó đảm bảo rằng hệ thống không bao giờ tự động đấu giá chống lại chính mình
          AND max_bid_amount >= (p_bid_amount + bid_increment)  ----------------> đảm bảo rằng số tiền tôi đa người dùng có thể bỏ ra phải lớn hơn giá đồ vật hiện tại + với bước nhảy tiền mà người dùng cài đặt

        LIMIT 1; chỉ lấy 1 phần tử ra ngoài 





  
        IF v_auto_user_id IS NOT NULL THEN  ------------> Nếu là NULL thì nghĩa là không có người khác cài đặt tự động đấu giá hoặc số tiền người đó không đủ nên nó ko vượt qua cád điều kiện trên kia và khi lấy dữ liệu ra thì nó là NULL và hệ thống sẽ nhảy thẳng xuống END , không làm gì cả . Giá đồ vật vẫn giữ nguyên 

            SET v_next_price = p_bid_amount + v_auto_step; -----------> set cái giá sắp tới của đồ vật = với bước nhảy của ng dùng + số tiền hiện tại của nó

            
     
            IF v_next_price > v_auto_max THEN -------------> -- Không được vượt quá mức tối đa người dùng cài đặt
                SET v_next_price = v_auto_max;
            END IF;

            INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount)
            VALUES (p_auction_id, v_auto_user_id, v_next_price);   ----------------> cập nhật lại thông tin vào bảng BID.... để sau này vễ biểu đồ và tăng tinh minh bạch


            UPDATE AUCTIONS 
            SET current_max_price = v_next_price 
            WHERE auction_id = p_auction_id;     cập nhật giá hiện tại của vật = với giá ta vừa tính
        END IF;      -----------> kết thúc


        COMMIT;    ----------> lưu thay đổi
    ELSE
        -- Nếu giá đặt thấp hơn hoặc phiên đấu giá đã đóng
        ROLLBACK;
    END IF;
    
END // 

DELIMITER ;








                                                      BẢNG DÀNH CHO AUTO BIT 
Stored Procedure :Là một nhóm các câu lệnh SQL được lưu trữ sẵn trong cơ sở dữ liệu và có thể được gọi thực thi thông qua tên
Đặc điểm: Có thể nhận tham số đầu vào (IN), trả về kết quả (OUT), hoặc kết hợp cả hai (INOUT)


Transaction:Là một nhóm các câu lệnh SQL được thực hiện như một đơn vị công việc duy nhất, đảm bảo tính toàn vẹn.
Sử dụng START TRANSACTION để bắt đầu, COMMIT để lưu thay đổi, ROLLBACK để hủy bỏ và quay về trạng thái cũ, và SAVEPOINT để đánh dấu điểm phục hồi.

ON DELETE CASCADE là nếu xoá phần tử này thì xoá nguyên cái dòng chứa phần tử đó

CREATE TABLE IF NOT EXISTS AUTO_BID_CONFIGS(
     auto_bid_id INT AUTO_INCREMENT PRIMARY KEY,
      user_id INT,
      auction_id INT,
      max_bid_amount DECIMAL(15, 2),
      bid_increment DECIMAL(15, 2),
      is_active BOOLEAN DEFAULT TRUE ,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
      FOREIGN KEY(user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ,
      FOREIGN KEY(auction_id ) REFERENCES AUCTIONS(auction_id) ON DELETE CASCADE,
      UNIQUE(user_id, auction_id)
 );
--------------------> tạo một bảng auto bid để người dùng ai có như cầu dùng auto bid sử dụng 



CREATE INDEX idx_autobid_lookup 
ON AUTO_BID_CONFIGS (auction_id, is_active, max_bid_amount);
CREATE INDEX idx_bid_auction 
ON BID_TRANSACTIONS (auction_id, bid_amount DESC);
CREATE INDEX idx_bid_user 
ON BID_TRANSACTIONS (bidder_id);
EXPLAIN SELECT user_id FROM AUTO_BID_CONFIGS 
WHERE auction_id = 1 AND is_active = TRUE;

-------------------> INDEX này là tạo một bảng lưu một phần thông tin của bảng gốc nó có tác dụng tăng tốc độ truy tìm dữ liệu đỡ cho hệ thống phải mò vào bảng cũ 


