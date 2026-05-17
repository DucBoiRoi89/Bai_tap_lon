CREATE DATABASE  IF NOT EXISTS `thanh` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `thanh`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: thanh
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `art`
--

DROP TABLE IF EXISTS `art`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `art` (
  `item_id` int NOT NULL,
  `author` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT 'Unknown',
  `creation_year` int DEFAULT NULL,
  `material` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dimensions` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_certified` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`item_id`),
  KEY `idx_art_author` (`author`),
  CONSTRAINT `art_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `art`
--

LOCK TABLES `art` WRITE;
/*!40000 ALTER TABLE `art` DISABLE KEYS */;
/*!40000 ALTER TABLE `art` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auctions`
--

DROP TABLE IF EXISTS `auctions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auctions` (
  `auction_id` int NOT NULL AUTO_INCREMENT,
  `item_id` int DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `starting_price` decimal(15,2) NOT NULL,
  `current_max_price` decimal(15,2) DEFAULT NULL,
  `status` enum('OPEN','RUNNING','FINISHED','CANCELED') COLLATE utf8mb4_unicode_ci DEFAULT 'OPEN',
  PRIMARY KEY (`auction_id`),
  KEY `item_id` (`item_id`),
  CONSTRAINT `auctions_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `chk_price_logic` CHECK ((`current_max_price` >= `starting_price`)),
  CONSTRAINT `chk_time_logic` CHECK ((`end_time` > `start_time`))
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auctions`
--

LOCK TABLES `auctions` WRITE;
/*!40000 ALTER TABLE `auctions` DISABLE KEYS */;
INSERT INTO `auctions` VALUES (1,1,'2026-03-25 14:27:43','2026-05-12 20:43:31',10000.00,425000.00,'FINISHED'),(2,1,'2026-04-18 11:30:34','2026-04-29 00:00:00',500000.00,50000000.00,'FINISHED'),(3,1,'2026-04-18 14:09:58','2026-04-18 15:09:58',1000.00,NULL,'FINISHED'),(4,1,'2026-04-18 14:11:53','2026-04-18 15:11:53',1000.00,NULL,'FINISHED'),(5,1,'2026-04-18 14:12:35','2026-04-18 15:12:35',1000.00,NULL,'FINISHED'),(6,1,'2026-04-18 14:13:56','2026-04-18 15:13:56',1000.00,NULL,'FINISHED'),(27,30,'2026-05-11 20:19:14','2026-05-11 23:59:00',555555.00,555555.00,'FINISHED'),(38,41,'2026-05-17 11:32:38','2026-05-17 11:36:36',10.00,100.00,'RUNNING');
/*!40000 ALTER TABLE `auctions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auto_bid_configs`
--

DROP TABLE IF EXISTS `auto_bid_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auto_bid_configs` (
  `config_id` int DEFAULT NULL,
  `auto_bid_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `auction_id` int DEFAULT NULL,
  `max_bid_amount` decimal(15,2) DEFAULT NULL,
  `bid_increment` decimal(15,2) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`auto_bid_id`),
  UNIQUE KEY `config_id` (`config_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_autobid_lookup` (`auction_id`,`is_active`,`max_bid_amount`),
  CONSTRAINT `auto_bid_configs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `auto_bid_configs_ibfk_2` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auto_bid_configs`
--

LOCK TABLES `auto_bid_configs` WRITE;
/*!40000 ALTER TABLE `auto_bid_configs` DISABLE KEYS */;
INSERT INTO `auto_bid_configs` VALUES (NULL,52,12,1,80000000.00,5000.00,1,NULL),(NULL,64,11,1,425000.00,5000.00,1,NULL),(NULL,89,58,38,100.00,1.00,1,NULL),(NULL,90,46,38,100.00,1.00,1,NULL);
/*!40000 ALTER TABLE `auto_bid_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bid_transactions`
--

DROP TABLE IF EXISTS `bid_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bid_transactions` (
  `bid_id` int NOT NULL AUTO_INCREMENT,
  `auction_id` int DEFAULT NULL,
  `bidder_id` int DEFAULT NULL,
  `bid_amount` decimal(15,2) NOT NULL,
  `bid_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bid_id`),
  KEY `idx_bid_auction` (`auction_id`,`bid_amount` DESC),
  KEY `idx_bid_user` (`bidder_id`),
  CONSTRAINT `bid_transactions_ibfk_1` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`),
  CONSTRAINT `bid_transactions_ibfk_2` FOREIGN KEY (`bidder_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=900 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bid_transactions`
--

LOCK TABLES `bid_transactions` WRITE;
/*!40000 ALTER TABLE `bid_transactions` DISABLE KEYS */;
INSERT INTO `bid_transactions` VALUES (38,1,11,20000.00,'2026-05-11 14:44:49'),(39,1,12,25000.00,'2026-05-11 14:44:49'),(45,1,11,30000.00,'2026-05-11 15:20:49'),(46,1,12,35000.00,'2026-05-11 15:20:49'),(47,1,11,40000.00,'2026-05-11 15:20:49'),(48,1,12,45000.00,'2026-05-11 15:20:49'),(49,1,11,50000.00,'2026-05-11 15:20:49'),(50,1,12,55000.00,'2026-05-11 15:20:49'),(51,1,11,60000.00,'2026-05-11 15:20:49'),(52,1,12,65000.00,'2026-05-11 15:20:49'),(53,1,11,70000.00,'2026-05-11 15:20:49'),(54,1,12,75000.00,'2026-05-11 15:20:49'),(55,1,11,80000.00,'2026-05-11 15:20:49'),(56,1,12,85000.00,'2026-05-11 15:20:49'),(57,1,11,90000.00,'2026-05-11 15:20:50'),(58,1,12,95000.00,'2026-05-11 15:20:50'),(59,1,11,100000.00,'2026-05-11 15:20:50'),(60,1,12,105000.00,'2026-05-11 15:20:50'),(61,1,11,110000.00,'2026-05-11 15:20:50'),(62,1,12,115000.00,'2026-05-11 15:20:50'),(63,1,11,120000.00,'2026-05-11 15:20:50'),(64,1,12,125000.00,'2026-05-11 15:20:50'),(65,1,11,130000.00,'2026-05-11 15:22:16'),(66,1,12,135000.00,'2026-05-11 15:22:16'),(67,1,11,140000.00,'2026-05-11 15:22:17'),(68,1,12,145000.00,'2026-05-11 15:22:17'),(69,1,11,150000.00,'2026-05-11 15:22:18'),(70,1,12,155000.00,'2026-05-11 15:22:18'),(71,1,11,160000.00,'2026-05-11 15:22:19'),(72,1,12,165000.00,'2026-05-11 15:22:19'),(73,1,11,170000.00,'2026-05-11 15:22:20'),(74,1,12,175000.00,'2026-05-11 15:22:21'),(75,1,11,180000.00,'2026-05-11 15:22:21'),(76,1,12,185000.00,'2026-05-11 15:22:22'),(77,1,11,190000.00,'2026-05-11 15:22:22'),(78,1,12,195000.00,'2026-05-11 15:22:23'),(79,1,11,200000.00,'2026-05-11 15:22:23'),(80,1,12,205000.00,'2026-05-11 15:22:24'),(81,1,11,210000.00,'2026-05-11 15:22:24'),(82,1,12,215000.00,'2026-05-11 15:22:25'),(83,1,11,220000.00,'2026-05-11 15:22:25'),(84,1,12,225000.00,'2026-05-11 15:22:26'),(85,1,11,230000.00,'2026-05-11 15:26:53'),(86,1,12,235000.00,'2026-05-11 15:26:54'),(87,1,11,240000.00,'2026-05-11 15:26:54'),(88,1,12,245000.00,'2026-05-11 15:26:55'),(89,1,11,250000.00,'2026-05-11 15:26:56'),(90,1,12,255000.00,'2026-05-11 15:26:56'),(91,1,11,260000.00,'2026-05-11 15:26:57'),(92,1,12,265000.00,'2026-05-11 15:26:57'),(93,1,11,270000.00,'2026-05-11 15:26:58'),(94,1,12,275000.00,'2026-05-11 15:26:58'),(95,1,11,280000.00,'2026-05-11 15:26:59'),(96,1,12,285000.00,'2026-05-11 15:26:59'),(97,1,11,290000.00,'2026-05-11 15:27:00'),(98,1,12,295000.00,'2026-05-11 15:27:00'),(99,1,11,300000.00,'2026-05-11 15:27:01'),(100,1,12,305000.00,'2026-05-11 15:27:01'),(101,1,11,310000.00,'2026-05-11 15:27:02'),(102,1,12,315000.00,'2026-05-11 15:27:03'),(103,1,11,320000.00,'2026-05-11 15:27:03'),(104,1,12,325000.00,'2026-05-11 15:27:04'),(105,1,11,330000.00,'2026-05-11 15:28:05'),(106,1,12,335000.00,'2026-05-11 15:28:06'),(107,1,11,340000.00,'2026-05-11 15:28:06'),(108,1,12,345000.00,'2026-05-11 15:28:07'),(109,1,11,350000.00,'2026-05-11 15:28:07'),(110,1,12,355000.00,'2026-05-11 15:28:08'),(111,1,11,360000.00,'2026-05-11 15:28:08'),(112,1,12,365000.00,'2026-05-11 15:28:09'),(113,1,11,370000.00,'2026-05-11 15:28:10'),(114,1,12,375000.00,'2026-05-11 15:28:10'),(115,1,11,380000.00,'2026-05-11 15:28:11'),(116,1,12,385000.00,'2026-05-11 15:28:11'),(117,1,11,390000.00,'2026-05-11 15:28:12'),(118,1,12,395000.00,'2026-05-11 15:28:12'),(119,1,11,400000.00,'2026-05-11 15:28:13'),(120,1,12,405000.00,'2026-05-11 15:28:13'),(121,1,11,410000.00,'2026-05-11 15:28:14'),(122,1,12,415000.00,'2026-05-11 15:28:14'),(123,1,11,420000.00,'2026-05-11 15:28:15'),(124,1,12,425000.00,'2026-05-11 15:28:15'),(811,38,58,11.00,'2026-05-17 04:32:56'),(812,38,58,13.00,'2026-05-17 04:33:03'),(813,38,46,14.00,'2026-05-17 04:33:28'),(814,38,58,15.00,'2026-05-17 04:33:29'),(815,38,46,16.00,'2026-05-17 04:33:29'),(816,38,58,17.00,'2026-05-17 04:33:30'),(817,38,46,18.00,'2026-05-17 04:33:30'),(818,38,58,19.00,'2026-05-17 04:33:31'),(819,38,46,20.00,'2026-05-17 04:33:31'),(820,38,58,21.00,'2026-05-17 04:33:32'),(821,38,46,22.00,'2026-05-17 04:33:32'),(822,38,58,23.00,'2026-05-17 04:33:33'),(823,38,46,24.00,'2026-05-17 04:33:33'),(824,38,58,25.00,'2026-05-17 04:33:34'),(825,38,46,26.00,'2026-05-17 04:33:34'),(826,38,58,27.00,'2026-05-17 04:33:35'),(827,38,46,28.00,'2026-05-17 04:33:35'),(828,38,58,29.00,'2026-05-17 04:33:36'),(829,38,46,30.00,'2026-05-17 04:33:36'),(830,38,58,31.00,'2026-05-17 04:33:37'),(831,38,46,32.00,'2026-05-17 04:33:38'),(832,38,58,33.00,'2026-05-17 04:33:38'),(833,38,46,34.00,'2026-05-17 04:33:39'),(834,38,58,35.00,'2026-05-17 04:33:39'),(835,38,46,36.00,'2026-05-17 04:33:40'),(836,38,58,37.00,'2026-05-17 04:33:40'),(837,38,46,38.00,'2026-05-17 04:33:41'),(838,38,58,39.00,'2026-05-17 04:33:41'),(839,38,46,40.00,'2026-05-17 04:33:42'),(840,38,58,41.00,'2026-05-17 04:33:42'),(841,38,46,42.00,'2026-05-17 04:33:43'),(842,38,58,43.00,'2026-05-17 04:33:43'),(843,38,46,44.00,'2026-05-17 04:33:44'),(844,38,58,45.00,'2026-05-17 04:33:44'),(845,38,46,46.00,'2026-05-17 04:33:45'),(846,38,58,47.00,'2026-05-17 04:33:45'),(847,38,46,48.00,'2026-05-17 04:33:46'),(848,38,58,49.00,'2026-05-17 04:33:46'),(849,38,46,50.00,'2026-05-17 04:33:47'),(850,38,58,51.00,'2026-05-17 04:33:47'),(851,38,46,52.00,'2026-05-17 04:33:48'),(852,38,58,53.00,'2026-05-17 04:33:49'),(853,38,46,54.00,'2026-05-17 04:33:49'),(854,38,58,55.00,'2026-05-17 04:33:50'),(855,38,46,56.00,'2026-05-17 04:33:50'),(856,38,58,57.00,'2026-05-17 04:33:51'),(857,38,46,58.00,'2026-05-17 04:33:51'),(858,38,58,59.00,'2026-05-17 04:33:52'),(859,38,46,60.00,'2026-05-17 04:33:52'),(860,38,58,61.00,'2026-05-17 04:33:53'),(861,38,46,62.00,'2026-05-17 04:33:53'),(862,38,58,63.00,'2026-05-17 04:33:54'),(863,38,46,64.00,'2026-05-17 04:33:54'),(864,38,58,65.00,'2026-05-17 04:33:55'),(865,38,46,66.00,'2026-05-17 04:33:55'),(866,38,58,67.00,'2026-05-17 04:33:56'),(867,38,46,68.00,'2026-05-17 04:33:56'),(868,38,58,69.00,'2026-05-17 04:33:57'),(869,38,46,70.00,'2026-05-17 04:33:57'),(870,38,58,71.00,'2026-05-17 04:33:58'),(871,38,46,72.00,'2026-05-17 04:33:59'),(872,38,58,73.00,'2026-05-17 04:33:59'),(873,38,46,74.00,'2026-05-17 04:34:00'),(874,38,58,75.00,'2026-05-17 04:34:00'),(875,38,46,76.00,'2026-05-17 04:34:01'),(876,38,58,77.00,'2026-05-17 04:34:01'),(877,38,46,78.00,'2026-05-17 04:34:02'),(878,38,58,79.00,'2026-05-17 04:34:02'),(879,38,46,80.00,'2026-05-17 04:34:03'),(880,38,58,81.00,'2026-05-17 04:34:03'),(881,38,46,82.00,'2026-05-17 04:34:04'),(882,38,58,83.00,'2026-05-17 04:34:04'),(883,38,46,84.00,'2026-05-17 04:34:05'),(884,38,58,85.00,'2026-05-17 04:34:05'),(885,38,46,86.00,'2026-05-17 04:34:06'),(886,38,58,87.00,'2026-05-17 04:34:06'),(887,38,46,88.00,'2026-05-17 04:34:07'),(888,38,58,89.00,'2026-05-17 04:34:07'),(889,38,46,90.00,'2026-05-17 04:34:08'),(890,38,58,91.00,'2026-05-17 04:34:09'),(891,38,46,92.00,'2026-05-17 04:34:09'),(892,38,58,93.00,'2026-05-17 04:34:10'),(893,38,46,94.00,'2026-05-17 04:34:10'),(894,38,58,95.00,'2026-05-17 04:34:11'),(895,38,46,96.00,'2026-05-17 04:34:11'),(896,38,58,97.00,'2026-05-17 04:34:12'),(897,38,46,98.00,'2026-05-17 04:34:12'),(898,38,58,99.00,'2026-05-17 04:34:13'),(899,38,46,100.00,'2026-05-17 04:34:13');
/*!40000 ALTER TABLE `bid_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `electronics`
--

DROP TABLE IF EXISTS `electronics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `electronics` (
  `item_id` int NOT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `warranty_months` int DEFAULT '0',
  `condition_status` enum('NEW','LIKE NEW','USED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `technical_specs` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`item_id`),
  KEY `idx_electronics_brand` (`brand`),
  CONSTRAINT `electronics_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `electronics`
--

LOCK TABLES `electronics` WRITE;
/*!40000 ALTER TABLE `electronics` DISABLE KEYS */;
INSERT INTO `electronics` VALUES (4,'Apple','M3 2024',0,'LIKE NEW',NULL),(5,'Apple','M3 2024',0,'LIKE NEW',NULL),(6,'Apple','M3 2024',0,'LIKE NEW',NULL),(30,'1213123',NULL,121,NULL,NULL);
/*!40000 ALTER TABLE `electronics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `item_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seller_id` int DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  KEY `seller_id` (`seller_id`),
  CONSTRAINT `items_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (1,'R900P2025','RTX 5060','laptopgaming',1),(2,'R900P2025','RTX 5060','laptopgaming',1),(3,'DUREX','0,12mm','bao cao su',1),(4,'MacBook Pro M3','Hàng mới 99%','Electronics',1),(5,'MacBook Pro M3','Hàng mới 99%','Electronics',1),(6,'MacBook Pro M3','Hàng mới 99%','Electronics',1),(30,'thanh','','ELECTRONICS',49),(41,'xe máy','','VEHICLE',41);
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','SELLER','BIDDER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `balance` decimal(15,2) DEFAULT '0.00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'thanh123','thanhttvq123','TRAN TUAN THANH','ADMIN','2026-03-25 07:27:43',100000000.00),(3,'thanh3','thanhttvq23','TRAN TUAN THANH','ADMIN','2026-04-12 08:21:12',100000000.00),(11,'test_user_11','123',NULL,'BIDDER','2026-05-11 13:52:10',100000000.00),(12,'bot_auto_12','123456','Robot AutoBid 12','BIDDER','2026-05-11 13:43:31',99575000.00),(32,'thanhttvq','THanhttvq@123','Tran Tuan Thanh','ADMIN','2026-04-18 04:22:12',100000000.00),(33,'thanh_seller','123','Thanh Chủ Hàng','SELLER','2026-04-18 07:09:57',100000000.00),(34,'bidder_1','123','Nguyễn Văn A','BIDDER','2026-04-18 07:09:57',100000000.00),(35,'bidder_2','123','Trần Thị B','BIDDER','2026-04-18 07:09:57',100000000.00),(40,'thanhttvq12','123456789','Trần Tuấn Thành','SELLER','2026-05-10 05:06:26',100000000.00),(41,'thanh','123456789','Thành','SELLER','2026-05-10 05:07:29',99999998.00),(42,'thanh12','123','thanh12','BIDDER','2026-05-10 12:40:45',99999899.00),(43,'thanh1234','123','thanh123','BIDDER','2026-05-11 05:18:47',100000000.00),(44,'thanhttvq1','123','Thành','BIDDER','2026-05-11 05:19:44',100000000.00),(45,'admin','123','Quản trị viên Hệ thống','ADMIN','2026-05-11 08:02:06',99872413.00),(46,'typhu','123','Đại gia Đấu giá','BIDDER','2026-05-11 08:02:06',98875178.00),(47,' minh','123','Minh óc chó','BIDDER','2026-05-11 13:08:15',100000000.00),(48,'minh1','123','Minh ngu','BIDDER','2026-05-11 13:08:56',1360.00),(49,'minh2','123','Minh ocs ','SELLER','2026-05-11 13:17:49',100000000.00),(50,'thanhttvq123','123','thanh123','BIDDER','2026-05-12 06:34:57',10000000.00),(51,'tt','1','ttt','SELLER','2026-05-12 07:32:08',0.00),(52,'Minh','123','minh','BIDDER','2026-05-12 16:36:50',50000000.00),(53,'Minh ngu','123','Minh óc ','BIDDER','2026-05-12 16:38:04',50000000.00),(54,'óc','123','Minh','SELLER','2026-05-12 16:51:15',0.00),(55,'thanh4','123','thanhttvq','BIDDER','2026-05-13 00:22:17',50000000.00),(56,'duc','123','duyc','BIDDER','2026-05-15 09:36:25',50000000.00),(57,'thanh12345','123','thanh','SELLER','2026-05-17 04:11:51',0.00),(58,'minh123','123','Nguyễn Ngọc Hồng Minh','BIDDER','2026-05-17 04:31:23',50000000.00);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `item_id` int NOT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model_year` int DEFAULT NULL,
  `mileage` int DEFAULT '0',
  `license_plate` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fuel_type` enum('Gasoline','Diesel','Electric') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  KEY `idx_vehicle_brand` (`brand`),
  CONSTRAINT `vehicles_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
/*!40000 ALTER TABLE `vehicles` DISABLE KEYS */;
INSERT INTO `vehicles` VALUES (41,'wave',NULL,10,'98 1111',NULL);
/*!40000 ALTER TABLE `vehicles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-17 22:38:09
