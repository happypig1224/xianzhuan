-- ============================================================
-- 闲置优品流转平台 - 完整数据库脚本
-- Database: xianzhuan-platform
-- Charset: utf8mb4
-- Author: Wu, Hui Ming
-- Date: 2026-06-07
-- ============================================================
CREATE DATABASE IF NOT EXISTS `xianzhuan_platform`;
USE `xianzhuan_platform`;
SET NAMES utf8mb4;
SET TIME_ZONE = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `openid`      VARCHAR(45)  DEFAULT NULL COMMENT '微信用户唯一标识',
    `name`        VARCHAR(32)  DEFAULT NULL COMMENT '姓名',
    `phone`       VARCHAR(11)  DEFAULT NULL COMMENT '手机号',
    `sex`         VARCHAR(2)   DEFAULT NULL COMMENT '性别',
    `id_number`   VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME     DEFAULT NULL COMMENT '注册时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户信息';

-- ============================================================
-- 管理员表
-- ============================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(32) NOT NULL COMMENT '用户名',
    `name`        VARCHAR(32) NOT NULL COMMENT '姓名',
    `password`    VARCHAR(64) NOT NULL COMMENT '密码',
    `phone`       VARCHAR(11) NOT NULL COMMENT '手机号',
    `sex`         VARCHAR(2)  NOT NULL COMMENT '性别',
    `id_number`   VARCHAR(18) NOT NULL COMMENT '身份证号',
    `status`      INT         NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    `create_user` BIGINT               DEFAULT NULL COMMENT '创建人',
    `update_user` BIGINT               DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='管理员信息';

INSERT INTO `admin`
VALUES (1, 'admin', '管理员', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', '1', '110101199003077394', 1,
        '2026-06-06 19:33:05', '2026-06-06 19:33:05', 1, 1);

-- ============================================================
-- 收货地址表
-- ============================================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT      NOT NULL COMMENT '用户id',
    `consignee`     VARCHAR(50)          DEFAULT NULL COMMENT '收货人',
    `sex`           VARCHAR(2)           DEFAULT NULL COMMENT '性别',
    `phone`         VARCHAR(11) NOT NULL COMMENT '手机号',
    `province_code` VARCHAR(12)          DEFAULT NULL COMMENT '省级区划编码',
    `province_name` VARCHAR(32)          DEFAULT NULL COMMENT '省级名称',
    `city_code`     VARCHAR(12)          DEFAULT NULL COMMENT '市级区划编码',
    `city_name`     VARCHAR(32)          DEFAULT NULL COMMENT '市级名称',
    `district_code` VARCHAR(12)          DEFAULT NULL COMMENT '区级区划编码',
    `district_name` VARCHAR(32)          DEFAULT NULL COMMENT '区级名称',
    `detail`        VARCHAR(200)         DEFAULT NULL COMMENT '详细地址',
    `label`         VARCHAR(100)         DEFAULT NULL COMMENT '标签(如:家/公司/学校)',
    `is_default`    TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '默认地址 0:否 1:是',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='收货地址';

-- ============================================================
-- 商品分类表
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`        TINYINT     NOT NULL COMMENT '分类类型 1:二手商品分类',
    `name`        VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      INT                  DEFAULT NULL COMMENT '分类状态 0:禁用 1:启用',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    `create_user` BIGINT               DEFAULT NULL COMMENT '创建人',
    `update_user` BIGINT               DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_category_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='商品分类';

-- ============================================================
-- 二手商品表
-- ============================================================
DROP TABLE IF EXISTS `used_item`;
CREATE TABLE `used_item`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `category_id`    BIGINT         NOT NULL COMMENT '商品分类id',
    `brand`          VARCHAR(64)    DEFAULT NULL COMMENT '品牌',
    `original_price` DECIMAL(10, 2) NOT NULL COMMENT '原价/专柜价',
    `current_price`  DECIMAL(10, 2) DEFAULT NULL COMMENT '现价/售价',
    `shipping_fee`   DECIMAL(10, 2) DEFAULT 0.00 COMMENT '运费',
    `condition`      VARCHAR(32)    DEFAULT '全新' COMMENT '成色: 全新/九成新/八成新/七成新/六成新/五成新以下',
    `seller_id`      BIGINT         DEFAULT NULL COMMENT '卖家ID',
    `views`          INT            DEFAULT 0 COMMENT '浏览量',
    `image`          VARCHAR(500)   DEFAULT NULL COMMENT '商品图片URL(多图逗号分隔)',
    `description`    VARCHAR(1000)  DEFAULT NULL COMMENT '描述信息',
    `sales_count`    INT            DEFAULT 0 COMMENT '销量',
    `status`         INT            DEFAULT 1 COMMENT '状态 0:下架 1:在售',
    `create_time`    DATETIME       DEFAULT NULL COMMENT '创建时间',
    `update_time`    DATETIME       DEFAULT NULL COMMENT '更新时间',
    `create_user`    BIGINT         DEFAULT NULL COMMENT '创建人',
    `update_user`    BIGINT         DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_seller_id` (`seller_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='二手商品';

-- ============================================================
-- 商品规格表
-- ============================================================
DROP TABLE IF EXISTS `item_spec`;
CREATE TABLE `item_spec`
(
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id` BIGINT NOT NULL COMMENT '商品ID',
    `name`    VARCHAR(32)  DEFAULT NULL COMMENT '规格名称',
    `value`   VARCHAR(255) DEFAULT NULL COMMENT '规格数据(list JSON)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='商品规格关系表';

-- ============================================================
-- 商品评价表
-- ============================================================
DROP TABLE IF EXISTS `item_review`;
CREATE TABLE `item_review`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id`     BIGINT   NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `order_id`    BIGINT        DEFAULT NULL COMMENT '订单ID',
    `rating`      INT      NOT NULL COMMENT '评分1-5',
    `content`     VARCHAR(500)  DEFAULT NULL COMMENT '评价内容',
    `images`      VARCHAR(1000) DEFAULT NULL COMMENT '评价图片URL(逗号分隔)',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='商品评价';

-- ============================================================
-- 购物车表
-- ============================================================
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(32)             DEFAULT NULL COMMENT '商品名称',
    `image`       VARCHAR(255)            DEFAULT NULL COMMENT '图片',
    `user_id`     BIGINT         NOT NULL COMMENT '用户id',
    `item_id`     BIGINT                  DEFAULT NULL COMMENT '商品id',
    `item_spec`   VARCHAR(255)            DEFAULT NULL COMMENT '规格',
    `number`      INT            NOT NULL DEFAULT 1 COMMENT '数量',
    `amount`      DECIMAL(10, 2) NOT NULL COMMENT '金额',
    `create_time` DATETIME                DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='购物车';

-- ============================================================
-- 订单表
-- 二手商品流转状态:
--   1:待付款  2:待发货  3:已发货  4:待收货
--   5:售后中  6:交易完成  7:交易关闭
-- ============================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`
(
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `number`                  VARCHAR(50)             DEFAULT NULL COMMENT '订单号',
    `status`                  INT            NOT NULL DEFAULT 1 COMMENT '订单状态 1待付款 2待发货 3已发货 4待收货 5售后中 6交易完成 7交易关闭',
    `user_id`                 BIGINT         NOT NULL COMMENT '买家ID',
    `seller_id`               BIGINT                  DEFAULT NULL COMMENT '卖家ID',
    `address_id`              BIGINT         NOT NULL COMMENT '地址ID',
    `order_time`              DATETIME       NOT NULL COMMENT '下单时间',
    `checkout_time`           DATETIME                DEFAULT NULL COMMENT '成交/结账时间',
    `pay_method`              INT            NOT NULL DEFAULT 1 COMMENT '支付方式 1:微信 2:支付宝',
    `pay_status`              TINYINT        NOT NULL DEFAULT 0 COMMENT '支付状态 0:未支付 1:已支付 2:退款',
    `amount`                  DECIMAL(10, 2) NOT NULL COMMENT '实收金额',
    `remark`                  VARCHAR(100)            DEFAULT NULL COMMENT '备注',
    `phone`                   VARCHAR(11)             DEFAULT NULL COMMENT '手机号',
    `address`                 VARCHAR(255)            DEFAULT NULL COMMENT '地址',
    `user_name`               VARCHAR(32)             DEFAULT NULL COMMENT '用户名称',
    `consignee`               VARCHAR(32)             DEFAULT NULL COMMENT '收货人',
    `cancel_reason`           VARCHAR(255)            DEFAULT NULL COMMENT '取消原因',
    `rejection_reason`        VARCHAR(255)            DEFAULT NULL COMMENT '拒绝原因',
    `cancel_time`             DATETIME                DEFAULT NULL COMMENT '取消时间',
    `estimated_delivery_time` DATETIME                DEFAULT NULL COMMENT '预计送达时间',
    `shipping_status`         INT                     DEFAULT 1 COMMENT '配送状态 1:待发货 2:已发货 3:已收货',
    `shipping_time`           DATETIME                DEFAULT NULL COMMENT '发货时间',
    `version`                 INT                     DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_order` (`user_id`, `number`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='订单表';

-- ============================================================
-- 订单明细表
-- ============================================================
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail`
(
    `id`        BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`      VARCHAR(32)             DEFAULT NULL COMMENT '商品名称',
    `image`     VARCHAR(255)            DEFAULT NULL COMMENT '图片',
    `order_id`  BIGINT         NOT NULL COMMENT '订单id',
    `item_id`   BIGINT                  DEFAULT NULL COMMENT '商品id',
    `item_spec` VARCHAR(255)            DEFAULT NULL COMMENT '规格',
    `number`    INT            NOT NULL DEFAULT 1 COMMENT '数量',
    `amount`    DECIMAL(10, 2) NOT NULL COMMENT '金额',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='订单明细表';

-- ============================================================
-- 闪购商品表
-- ============================================================
DROP TABLE IF EXISTS `flash_deal`;
CREATE TABLE `flash_deal`
(
    `id`           BIGINT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    `voucher_id`   BIGINT           NOT NULL COMMENT '闪购商品ID',
    `title`        VARCHAR(255)     NOT NULL COMMENT '标题',
    `rules`        VARCHAR(1024)             DEFAULT NULL COMMENT '使用规则',
    `pay_value`    BIGINT UNSIGNED  NOT NULL COMMENT '支付金额(分), 如200表示2元',
    `actual_value` BIGINT           NOT NULL COMMENT '抵扣金额(分), 如200表示2元',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1:上架 2:下架 3:过期',
    `stock`        INT              NOT NULL COMMENT '库存',
    `begin_time`   DATETIME         NOT NULL COMMENT '生效时间',
    `end_time`     DATETIME         NOT NULL COMMENT '失效时间',
    `create_time`  DATETIME         NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1780812075171
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='闪购商品';
INSERT INTO `flash_deal` (`id`,
                          `voucher_id`,
                          `title`,
                          `rules`,
                          `pay_value`,
                          `actual_value`,
                          `status`,
                          `stock`,
                          `begin_time`,
                          `end_time`,
                          `create_time`)
VALUES (1780812075171,
        10001,
        'iPhone 15 闪购特惠',
        '每人限购1台，仅限新用户参与',
        50000,
        30000,
        1,
        500,
        '2026-06-18 10:00:00',
        '2026-06-20 23:59:59',
        NOW());
INSERT INTO `flash_deal`
VALUES (1780812075170, 1001, '超值优惠券', '每人限购2件，仅限线下使用，不可与其他优惠叠加', 1990, 5000, 1, 100,
        '2026-06-07 10:00:00', '2026-06-14 22:00:00', '2026-06-07 14:01:15');

-- ============================================================
-- 闪购订单表
-- ============================================================
DROP TABLE IF EXISTS `flash_order`;
CREATE TABLE `flash_order`
(
    `id`          BIGINT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT           NOT NULL COMMENT '用户id',
    `deal_id`     BIGINT           NOT NULL COMMENT '闪购商品ID',
    `order_time`  DATETIME         NOT NULL COMMENT '下单时间',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1:待付款 2:已付款 3:已取消 4:已退款 5:支付失败 6:已删除',
    `create_time` DATETIME         NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='闪购订单表';

-- ============================================================
-- 用户消息表
-- ============================================================
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content`     VARCHAR(500) NOT NULL COMMENT '消息内容',
    `type`        TINYINT DEFAULT 0 COMMENT '消息类型 0:系统通知 1:订单消息 2:商品消息',
    `is_read`     TINYINT DEFAULT 0 COMMENT '是否已读 0:未读 1:已读',
    `create_time` DATETIME     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='站内消息';

-- ============================================================
-- 本地消息表（可靠消息投递）
-- ============================================================
DROP TABLE IF EXISTS `local_message`;
CREATE TABLE `local_message`
(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `business_id`       BIGINT      NOT NULL COMMENT '业务ID',
    `message_type`      VARCHAR(50) NOT NULL COMMENT '消息类型',
    `content`           TEXT COMMENT '消息内容',
    `status`            VARCHAR(20) DEFAULT 'PENDING' COMMENT '消息状态',
    `retry_count`       INT         DEFAULT 0 COMMENT '重试次数',
    `max_retry_count`   INT         DEFAULT 3 COMMENT '最大重试次数',
    `next_execute_time` DATETIME    DEFAULT NULL COMMENT '下次执行时间',
    `create_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `process_time`      DATETIME    DEFAULT NULL COMMENT '处理完成时间',
    `error_message`     TEXT COMMENT '错误信息',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='本地消息表';

-- ============================================================
-- AI聊天记忆表
-- ============================================================
DROP TABLE IF EXISTS `chat_memory`;
CREATE TABLE `chat_memory`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id`   VARCHAR(512) NOT NULL COMMENT '会话ID',
    `message_type` VARCHAR(20)  NOT NULL COMMENT '消息类型 user:用户消息 assistant:助手消息',
    `content`      TEXT         NOT NULL COMMENT '消息内容',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    PRIMARY KEY (`id`),
    KEY `idx_user_session_id` (`session_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI聊天记录表';

-- ============================================================
-- 恢复设置
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;