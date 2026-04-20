-- CRM 订单模块增量脚本

CREATE TABLE IF NOT EXISTS `wk_crm_order` (
  `order_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单标题',
  `order_status` int(0) DEFAULT 0 COMMENT '订单状态',
  `exchange_rate` decimal(12,6) DEFAULT 1.000000 COMMENT '汇率换算',
  `quote_amount` decimal(10,2) DEFAULT 0.00 COMMENT '报价金额',
  `purchase_cost` decimal(10,2) DEFAULT 0.00 COMMENT '采购成本',
  `logistics_cost` decimal(10,2) DEFAULT 0.00 COMMENT '物流成本',
  `handling_fee_cost` decimal(10,2) DEFAULT 0.00 COMMENT '平手续成本',
  `consumable_cost` decimal(10,2) DEFAULT 0.00 COMMENT '耗材成本',
  `other_cost` decimal(10,2) DEFAULT 0.00 COMMENT '其他成本',
  `profit_amount` decimal(10,2) DEFAULT 0.00 COMMENT '利润金额',
  `profit_rate` decimal(10,2) DEFAULT 0.00 COMMENT '利润率',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
  `owner_user_id` bigint(0) DEFAULT NULL COMMENT '负责人',
  `create_user_id` bigint(0) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `batch_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批次ID',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE KEY `uk_order_number` (`order_number`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单表' ROW_FORMAT=Dynamic;

CREATE TABLE IF NOT EXISTS `wk_crm_order_data` (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `field_id` int(0) DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段名称',
  `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '字段值',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `batch_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批次ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_data_batch_id` (`batch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单扩展字段表' ROW_FORMAT=Dynamic;

CREATE TABLE IF NOT EXISTS `wk_crm_order_relation` (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `order_id` int(0) NOT NULL COMMENT '订单ID',
  `relation_type` int(0) NOT NULL COMMENT '关联模块类型',
  `relation_id` int(0) NOT NULL COMMENT '关联模块ID',
  `relation_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联名称快照',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_relation_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_relation_type_id` (`relation_type`, `relation_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单关联关系表' ROW_FORMAT=Dynamic;

CREATE TABLE IF NOT EXISTS `wk_crm_order_product` (
  `r_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` int(0) NOT NULL COMMENT '订单ID',
  `product_id` int(0) DEFAULT NULL COMMENT '产品ID',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '产品名称快照',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '产品标准单价',
  `sales_price` decimal(10,2) DEFAULT 0.00 COMMENT '报价单价',
  `num` decimal(10,2) DEFAULT 0.00 COMMENT '数量',
  `discount` decimal(10,2) DEFAULT 100.00 COMMENT '折扣',
  `subtotal` decimal(10,2) DEFAULT 0.00 COMMENT '报价小计',
  `purchase_price` decimal(10,2) DEFAULT 0.00 COMMENT '采购单价',
  `purchase_cost` decimal(10,2) DEFAULT 0.00 COMMENT '采购成本',
  `logistics_cost` decimal(10,2) DEFAULT 0.00 COMMENT '物流成本',
  `profit_amount` decimal(10,2) DEFAULT 0.00 COMMENT '利润金额',
  `profit_rate` decimal(10,2) DEFAULT 0.00 COMMENT '利润率',
  `unit` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '明细备注',
  `sort` int(0) DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`r_id`) USING BTREE,
  KEY `idx_order_product_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_product_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单报价利润明细表' ROW_FORMAT=Dynamic;

INSERT INTO `wk_admin_config` (`config_id`, `status`, `name`, `value`, `description`)
SELECT 262462, 0, 'numberSetting', '19', '自动编号设置'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `wk_admin_config` WHERE `name` = 'numberSetting' AND `value` = '19'
);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 943, 1, '订单管理', 'order', NULL, NULL, 1, 11, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 943);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 944, 943, '新建', 'save', '/crmOrder/add', NULL, 3, 1, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 944);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 945, 943, '编辑', 'update', '/crmOrder/update', NULL, 3, 2, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 945);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 946, 943, '查看列表', 'index', '/crmOrder/queryPageList,/crmOrder/queryPageListByRelation,/crmOrder/querySimpleEntity', NULL, 3, 3, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 946);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 947, 943, '查看详情', 'read', '/crmOrder/queryById/*,/crmOrder/information/*,/crmOrder/queryFileList,/crmOrder/queryProductList/*,/crmOrder/queryQuotationList/*,/crmOrder/queryProfitList/*', NULL, 3, 4, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 947);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 948, 943, '删除', 'delete', '/crmOrder/deleteByIds', NULL, 3, 5, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 948);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 949, 943, '导入', 'excelimport', '/crmOrder/downloadExcel,/crmOrder/uploadExcel', NULL, 3, 6, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 949);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 950, 943, '导出', 'excelexport', '/crmOrder/allExportExcel,/crmOrder/batchExportExcel', NULL, 3, 7, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 950);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101914, 'order_number', '订单编号', 1, 19, NULL, NULL, 255, '', 1, 1, 0, NULL, 176, 0, NOW(), 1, NULL, 50, NULL, '0,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101914);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101915, 'title', '订单标题', 1, 19, NULL, NULL, 255, '', 0, 1, 1, NULL, 176, 0, NOW(), 1, NULL, 50, NULL, '0,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101915);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101916, 'order_status', '订单状态', 3, 19, NULL, NULL, NULL, '', 0, 0, 2, '草稿,报价中,已成交,已关闭', 176, 0, NOW(), 1, NULL, 50, NULL, '1,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101916);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101926, 'exchange_rate', '汇率换算', 6, 19, NULL, NULL, NULL, '1', 0, 0, 3, NULL, 176, 0, NOW(), 1, NULL, 50, 6, '1,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101926);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101917, 'quote_amount', '报价金额', 6, 19, NULL, NULL, NULL, '', 0, 0, 4, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '2,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101917);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101918, 'purchase_cost', '采购成本', 6, 19, NULL, NULL, NULL, '', 0, 0, 5, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '2,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101918);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101925, 'logistics_cost', '物流成本', 6, 19, NULL, NULL, NULL, '', 0, 0, 6, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '3,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101925);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101927, 'handling_fee_cost', '平手续成本', 6, 19, NULL, NULL, NULL, '', 0, 0, 7, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '3,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101927);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101928, 'consumable_cost', '耗材成本', 6, 19, NULL, NULL, NULL, '', 0, 0, 8, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '4,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101928);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101929, 'other_cost', '其他成本', 6, 19, NULL, NULL, NULL, '', 0, 0, 9, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '4,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101929);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101919, 'profit_amount', '利润金额', 6, 19, NULL, NULL, NULL, '', 0, 0, 10, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '5,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101919);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101920, 'profit_rate', '利润率', 42, 19, NULL, NULL, NULL, '', 0, 0, 11, NULL, 176, 0, NOW(), 1, NULL, 50, 2, '5,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101920);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101923, 'remark', '备注', 2, 19, NULL, NULL, 1000, '', 0, 0, 12, NULL, 176, 0, NOW(), 1, NULL, 50, NULL, '6,0', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101923);

INSERT INTO `wk_crm_field`
(`field_id`, `field_name`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `is_hidden`, `update_time`, `field_type`, `relevant`, `style_percent`, `precisions`, `form_position`, `max_num_restrict`, `min_num_restrict`, `form_assist_id`)
SELECT 1101924, 'owner_user_id', '负责人', 28, 19, NULL, NULL, NULL, '', 0, 1, 13, NULL, 176, 0, NOW(), 1, NULL, 50, NULL, '6,1', NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_crm_field` WHERE `field_id` = 1101924);
