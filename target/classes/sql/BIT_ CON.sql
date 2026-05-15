USE thanh;
DELIMITER //
DROP PROCEDURE IF EXISTS PRO_PlaceSingleBid //
CREATE PROCEDURE PRO_PlaceSingleBid (
    IN p_bid_amount DECIMAL(15,2), 
    IN p_user_id INT, 
    IN p_auction_id INT,
    OUT p_status_code INT
)
BEGIN 
    DECLARE v_current_max_price DECIMAL(15,2); 
    DECLARE v_starting_price DECIMAL(15,2);
    DECLARE v_user_balance DECIMAL(15,2);
    DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED'); 
    DECLARE v_target_price DECIMAL(15,2);
    DECLARE v_previous_bidder_id INT DEFAULT NULL;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;

    SELECT starting_price, current_max_price, status 
    INTO v_starting_price, v_current_max_price, v_status
    FROM AUCTIONS WHERE auction_id = p_auction_id FOR UPDATE;
    
    -- Lấy ID người đang top 1 để hoàn tiền
    SELECT bidder_id INTO v_previous_bidder_id
    FROM BID_TRANSACTIONS WHERE auction_id = p_auction_id
    ORDER BY bid_amount DESC, bid_time ASC LIMIT 1;
    
    SELECT balance INTO v_user_balance 
    FROM USERS WHERE user_id = p_user_id FOR UPDATE;
    
    IF v_status = 'RUNNING' THEN
        IF v_current_max_price IS NULL THEN SET v_target_price = v_starting_price;
        ELSE SET v_target_price = v_current_max_price; END IF;

        IF v_user_balance < p_bid_amount THEN
            SET p_status_code = -3; -- Lỗi: Thiếu tiền
            ROLLBACK;
        ELSE IF p_bid_amount > v_target_price OR (v_current_max_price IS NULL AND p_bid_amount = v_starting_price) THEN
               
            -- 1. TRỪ TIỀN người đang đặt giá
            UPDATE USERS SET balance = balance - p_bid_amount WHERE user_id = p_user_id;
            
            -- 2. HOÀN TIỀN cho người cũ (Nếu có)
            IF v_previous_bidder_id IS NOT NULL THEN
                UPDATE USERS SET balance = balance + v_current_max_price WHERE user_id = v_previous_bidder_id;
            END IF;
            
            -- 3. CẬP NHẬT GIÁ MỚI
            INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount) VALUES (p_auction_id, p_user_id, p_bid_amount);
            UPDATE AUCTIONS SET current_max_price = p_bid_amount WHERE auction_id = p_auction_id;
            
            SET p_status_code = 1; -- Thành công
            COMMIT;
        ELSE
            SET p_status_code = 0; -- Lỗi: Giá thấp
            ROLLBACK;
        END IF;
    ELSE
        SET p_status_code = -1; -- Lỗi: Phiên đóng
        ROLLBACK;
    END IF;
END //
DELIMITER ;