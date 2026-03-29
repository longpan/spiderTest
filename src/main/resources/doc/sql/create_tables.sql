-- ----------------------------
-- Table structure for mh_pet_item
-- ----------------------------
DROP TABLE IF EXISTS `mh_pet_item`;
CREATE TABLE `mh_pet_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(255) DEFAULT NULL COMMENT '编号',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `level` varchar(255) DEFAULT NULL COMMENT '等级',
  `serverName` varchar(255) DEFAULT NULL COMMENT '服务器',
  `serverId` varchar(255) DEFAULT NULL COMMENT '服务器id',
  `price` varchar(255) DEFAULT NULL COMMENT '价格',
  `collect` varchar(255) DEFAULT NULL COMMENT '收藏人数',
  `valuationValue` decimal(18,2) DEFAULT NULL COMMENT '估值',
  `valuationScore` decimal(18,2) DEFAULT NULL COMMENT '性价比',
  `txt` text DEFAULT NULL COMMENT '评价',
  `lightSpot1` text DEFAULT NULL COMMENT '亮点1',
  `lightSpot2` text DEFAULT NULL COMMENT '亮点2',
  `skillNum` int(11) DEFAULT NULL COMMENT '技能数量',
  `skillList` text DEFAULT NULL COMMENT '技能列表',
  `blood` varchar(255) DEFAULT NULL COMMENT '气血',
  `corporeity` varchar(255) DEFAULT NULL COMMENT '体质',
  `magic` varchar(255) DEFAULT NULL COMMENT '魔法',
  `power` varchar(255) DEFAULT NULL COMMENT '法力',
  `attack` varchar(255) DEFAULT NULL COMMENT '攻击',
  `strength` varchar(255) DEFAULT NULL COMMENT '力量',
  `defense` varchar(255) DEFAULT NULL COMMENT '防御',
  `endurance` varchar(255) DEFAULT NULL COMMENT '耐力',
  `speed` varchar(255) DEFAULT NULL COMMENT '速度',
  `agility` varchar(255) DEFAULT NULL COMMENT '敏捷',
  `mageWound` varchar(255) DEFAULT NULL COMMENT '法伤',
  `potency` varchar(255) DEFAULT NULL COMMENT '潜能',
  `mageDefence` varchar(255) DEFAULT NULL COMMENT '法防',
  `attackQualification` varchar(255) DEFAULT NULL COMMENT '攻击资质',
  `lifetime` varchar(255) DEFAULT NULL COMMENT '寿命',
  `defenseQualification` varchar(255) DEFAULT NULL COMMENT '防御资质',
  `growUp` varchar(255) DEFAULT NULL COMMENT '成长',
  `physicalQualification` varchar(255) DEFAULT NULL COMMENT '体力资质',
  `manaQualification` varchar(255) DEFAULT NULL COMMENT '法力资质',
  `speedQualification` varchar(255) DEFAULT NULL COMMENT '速度资质',
  `dodgeQualification` varchar(255) DEFAULT NULL COMMENT '躲闪资质',
  `isBaby` varchar(255) DEFAULT NULL COMMENT '是否宝宝',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '最后修改时间',
  `detailUrl` text DEFAULT NULL COMMENT '链接',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='梦幻西游宠物表';

-- ----------------------------
-- Table structure for mh_lingshi_item
-- ----------------------------
DROP TABLE IF EXISTS `mh_lingshi_item`;
CREATE TABLE `mh_lingshi_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(255) DEFAULT NULL COMMENT '编号',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `level` varchar(255) DEFAULT NULL COMMENT '等级',
  `serverName` varchar(255) DEFAULT NULL COMMENT '服务器',
  `serverId` varchar(255) DEFAULT NULL COMMENT '服务器id',
  `price` varchar(255) DEFAULT NULL COMMENT '价格',
  `collect` varchar(255) DEFAULT NULL COMMENT '收藏人数',
  `lightSpot1` text DEFAULT NULL COMMENT '亮点1',
  `lightSpot2` text DEFAULT NULL COMMENT '亮点2',
  `primeAttribute` varchar(255) DEFAULT NULL COMMENT '主属性',
  `accessoryAttributeNum` int(11) DEFAULT NULL COMMENT '附属性数量',
  `accessoryAttribute` text DEFAULT NULL COMMENT '附属性',
  `specialEffects` varchar(255) DEFAULT NULL COMMENT '特效',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '最后修改时间',
  `detailUrl` text DEFAULT NULL COMMENT '链接',
  `valuationValue` decimal(18,2) DEFAULT NULL COMMENT '估值',
  `valuationScore` decimal(18,2) DEFAULT NULL COMMENT '性价比',
  `txt` text DEFAULT NULL COMMENT '评价',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='梦幻西游灵饰表';

-- ----------------------------
-- Table structure for mh_equip_item
-- ----------------------------
DROP TABLE IF EXISTS `mh_equip_item`;
CREATE TABLE `mh_equip_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(255) DEFAULT NULL COMMENT '编号',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `level` varchar(255) DEFAULT NULL COMMENT '等级',
  `serverName` varchar(255) DEFAULT NULL COMMENT '服务器',
  `serverId` varchar(255) DEFAULT NULL COMMENT '服务器id',
  `price` varchar(255) DEFAULT NULL COMMENT '价格',
  `collect` varchar(255) DEFAULT NULL COMMENT '收藏人数',
  `lightSpot1` text DEFAULT NULL COMMENT '亮点1',
  `lightSpot2` text DEFAULT NULL COMMENT '亮点2',
  `primeAttribute` varchar(255) DEFAULT NULL COMMENT '主属性',
  `accessoryAttributeNum` int(11) DEFAULT NULL COMMENT '附属性数量',
  `accessoryAttribute` text DEFAULT NULL COMMENT '附属性',
  `specialEffects` varchar(255) DEFAULT NULL COMMENT '特效',
  `runestones` text DEFAULT NULL COMMENT '符石',
  `descYellow` varchar(255) DEFAULT NULL COMMENT '黄字描述',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '最后修改时间',
  `detailUrl` text DEFAULT NULL COMMENT '链接',
  `valuationValue` decimal(18,2) DEFAULT NULL COMMENT '估值',
  `valuationScore` decimal(18,2) DEFAULT NULL COMMENT '性价比',
  `txt` text DEFAULT NULL COMMENT '评价',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='梦幻西游装备表';
