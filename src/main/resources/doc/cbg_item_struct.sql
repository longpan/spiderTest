/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50741 (5.7.41)
 Source Host           : localhost:3306
 Source Schema         : spider

 Target Server Type    : MySQL
 Target Server Version : 50741 (5.7.41)
 File Encoding         : 65001

 Date: 17/12/2024 23:49:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cbg_item
-- ----------------------------
DROP TABLE IF EXISTS `mh_pet_item`;
CREATE TABLE `mh_pet_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `level` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `publicity` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `bargin` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `serverName` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `price` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `txt` varchar(1024) COLLATE utf8_bin DEFAULT NULL,
  `collect` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `detailUrl` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `serverId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `createtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `draw` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4090 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;
