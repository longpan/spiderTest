DROP TABLE IF EXISTS `csdn_blog`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `title` varchar(50) DEFAULT NULL COMMENT '标题',
  `pref` varchar(50) DEFAULT NULL COMMENT '简介',
  `url` varchar(50)  COMMENT 'url',
  `read_count` varchar(50)  COMMENT 'readCount',
  `url` varchar(50)  COMMENT 'url',
  `creat_time` datetime DEFAULT NULL COMMENT '自定义填充的创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='博客表';