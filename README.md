DELIMITER //

DROP PROCEDURE IF EXISTS PRO_bitcon //

CREATE PROCEDURE PRO_bitcon (
    IN p_bid_amount DECIMAL(15,2), 
    IN p_user_id INT, 
    IN p_auction_id INT
)
BEGIN 
    DECLARE v_current_max_price DECIMAL(15,2); 
    DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED'); 
    DECLARE v_auto_user_id INT;
    DECLARE v_auto_max DECIMAL(15,2);
    DECLARE v_auto_step DECIMAL(15,2);
    DECLARE v_next_bid DECIMAL(15,2);
    DECLARE v_last_bidder_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
    END;

    START TRANSACTION;

    -- 1. Khóa phiên đấu giá để xử lý đồng thời (Concurrent Bidding) [cite: 115, 260]
    SELECT current_max_price, status 
    INTO v_current_max_price, v_status
    FROM AUCTIONS
    WHERE auction_id = p_auction_id FOR UPDATE;

    -- 2. Kiểm tra tính hợp lệ của giá bid ban đầu [cite: 115, 226]
    IF v_status = 'RUNNING' AND p_bid_amount > v_current_max_price THEN
        
        -- Lưu lượt bid đầu tiên
        INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount)
        VALUES (p_auction_id, p_user_id, p_bid_amount);
        
        SET v_current_max_price = p_bid_amount;
        SET v_last_bidder_id = p_user_id;

        -- 3. Vòng lặp Auto-bid: Hệ thống tự động đấu giá cho đến khi không còn ai đủ điều kiện 
        auto_loop: WHILE TRUE DO
            SET v_auto_user_id = NULL;

            -- Tìm người có auto-bid hợp lệ và không phải là người vừa mới bid
            SELECT user_id, max_bid_amount, bid_increment
            INTO v_auto_user_id, v_auto_max, v_auto_step
            FROM AUTO_BID_CONFIGS
            WHERE auction_id = p_auction_id
              AND is_active = TRUE
              AND user_id != v_last_bidder_id 
              AND max_bid_amount > v_current_max_price -- Còn khả năng nâng giá
            ORDER BY created_at ASC -- Ưu tiên người đăng ký trước [cite: 257]
            LIMIT 1;

            -- Nếu không còn ai cài auto-bid đủ điều kiện thì thoát vòng lặp
            IF v_auto_user_id IS NULL THEN
                LEAVE auto_loop;
            END IF;

            -- Tính toán giá bid tiếp theo
            SET v_next_bid = v_current_max_price + v_auto_step;
            
            -- Nếu bước giá vượt quá max_bid, chỉ lấy mức max_bid của họ [cite: 117, 256]
            IF v_next_bid > v_auto_max THEN
                SET v_next_bid = v_auto_max;
            END IF;

            -- Kiểm tra lại lần nữa: giá mới phải cao hơn giá hiện tại
            IF v_next_bid > v_current_max_price THEN
                INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount)
                VALUES (p_auction_id, v_auto_user_id, v_next_bid);
                
                SET v_current_max_price = v_next_bid;
                SET v_last_bidder_id = v_auto_user_id;
            ELSE
                -- Nếu không thể nâng giá cao hơn nữa (do chạm trần), kết thúc cấu hình này
                LEAVE auto_loop;
            END IF;
        END WHILE;

        -- 4. Cập nhật giá cuối cùng vào bảng AUCTIONS [cite: 115, 118]
        UPDATE AUCTIONS 
        SET current_max_price = v_current_max_price 
        WHERE auction_id = p_auction_id;

        COMMIT;
    ELSE
        ROLLBACK;
    END IF;
END // 

DELIMITER ;
