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
    DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED'); 
    DECLARE v_target_price DECIMAL(15,2);
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        SET p_status_code = -2;
    END;
    
    START TRANSACTION;

    -- Lấy cả giá khởi điểm
    SELECT starting_price, current_max_price, status 
    INTO v_starting_price, v_current_max_price, v_status
    FROM AUCTIONS
    WHERE auction_id = p_auction_id FOR UPDATE;
    
    IF v_status = 'RUNNING' THEN
        -- Xác định giá mốc để so sánh: nếu chưa ai bid thì so với giá khởi điểm
        IF v_current_max_price IS NULL THEN
            SET v_target_price = v_starting_price;
        ELSE
            SET v_target_price = v_current_max_price;
        END IF;

        IF p_bid_amount > v_target_price THEN
            INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount)
            VALUES (p_auction_id, p_user_id, p_bid_amount);
            
            UPDATE AUCTIONS 
            SET current_max_price = p_bid_amount 
            WHERE auction_id = p_auction_id;
            
            SET p_status_code = 1; 
            COMMIT;
        ELSE
            SET p_status_code = 0; -- Giá bid quá thấp
            ROLLBACK;
        END IF;
    ELSE
        SET p_status_code = -1; -- Phiên không hoạt động
        ROLLBACK;
    END IF;
END //
DELIMITER ;
