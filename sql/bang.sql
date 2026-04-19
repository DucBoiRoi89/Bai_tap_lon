CREATE DATABASE IF NOT EXISTS thanh 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE thanh;

CREATE TABLE IF NOT EXISTS USERS (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('ADMIN', 'SELLER', 'BIDDER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS ITEMS (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    seller_id INT,
    FOREIGN KEY (seller_id) REFERENCES USERS(user_id)
);
CREATE TABLE IF NOT EXISTS ELECTRONICS (
    item_id INT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,   
    model_name VARCHAR(100),         
    warranty_months INT DEFAULT 0,    
    condition_status ENUM('NEW', 'LIKE NEW', 'USED'), 
    technical_specs TEXT,           
    FOREIGN KEY (item_id) REFERENCES ITEMS(item_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS ART (
    item_id INT PRIMARY KEY,
    author VARCHAR(150) DEFAULT 'Unknown', 
    creation_year INT,                   
    material VARCHAR(100),                
    dimensions VARCHAR(50),              
    is_certified BOOLEAN DEFAULT FALSE,  
    FOREIGN KEY (item_id) REFERENCES ITEMS(item_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS VEHICLES (
    item_id INT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,     
    model_year INT,                   
    mileage INT DEFAULT 0,            
    license_plate VARCHAR(20),        
    fuel_type ENUM('Gasoline', 'Diesel', 'Electric'),
    FOREIGN KEY (item_id) REFERENCES ITEMS(item_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS AUCTIONS (
    auction_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    starting_price DECIMAL(15, 2) NOT NULL,
    current_max_price DECIMAL(15, 2),
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED') DEFAULT 'OPEN',
    FOREIGN KEY (item_id) REFERENCES ITEMS(item_id)
);
CREATE TABLE IF NOT EXISTS BID_TRANSACTIONS (
    bid_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT,
    bidder_id INT,
    bid_amount DECIMAL(15, 2) NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES AUCTIONS(auction_id),
    FOREIGN KEY (bidder_id) REFERENCES USERS(user_id)
);
CREATE TABLE IF NOT EXISTS AUTO_BID_CONFIGS (
    config_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT,
    user_id INT,
    max_bid_amount DECIMAL(15,2),
    bid_increment DECIMAL(15,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES AUCTIONS(auction_id),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);
DROP INDEX idx_art_author ON ART;
CREATE INDEX idx_art_author ON ART(author);

DROP INDEX idx_vehicle_brand ON VEHICLES;
CREATE INDEX idx_vehicle_brand ON VEHICLES(brand);

DROP INDEX idx_electronics_brand ON ELECTRONICS;
CREATE INDEX idx_electronics_brand ON ELECTRONICS(brand);
-- Cập nhật thông tin sản phẩm cơ bản
UPDATE ITEMS SET item_name = ?, description = ?, category = ? WHERE item_id = ?;
DELETE FROM ITEMS WHERE item_id = ?;
