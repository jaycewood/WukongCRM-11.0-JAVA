package com.kakarote.crm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 为已部署环境补齐订单模块新增字段，避免升级后还要手工执行 SQL。
 */
@Component
@Slf4j
public class OrderSchemaRepairRunner implements ApplicationRunner {

    private static final String ORDER_TABLE = "wk_crm_order";
    private static final String ORDER_PRODUCT_TABLE = "wk_crm_order_product";

    private final JdbcTemplate jdbcTemplate;

    public OrderSchemaRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureTables();
        repairOrderTable();
        repairOrderProductTable();
    }

    private void ensureTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `wk_crm_order` ("
                + "`order_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '订单ID',"
                + "`order_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',"
                + "`title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单标题',"
                + "`order_status` int(0) DEFAULT 0 COMMENT '订单状态',"
                + "`logistics_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流单号',"
                + "`exchange_rate` decimal(12,6) DEFAULT 1.000000 COMMENT '汇率换算',"
                + "`quote_amount` decimal(10,2) DEFAULT 0.00 COMMENT '报价金额',"
                + "`purchase_cost` decimal(10,2) DEFAULT 0.00 COMMENT '采购成本',"
                + "`logistics_cost` decimal(10,2) DEFAULT 0.00 COMMENT '物流成本',"
                + "`handling_fee_cost` decimal(10,2) DEFAULT 0.00 COMMENT '平手续成本',"
                + "`consumable_cost` decimal(10,2) DEFAULT 0.00 COMMENT '耗材成本',"
                + "`other_cost` decimal(10,2) DEFAULT 0.00 COMMENT '其他成本',"
                + "`profit_amount` decimal(10,2) DEFAULT 0.00 COMMENT '利润金额',"
                + "`profit_rate` decimal(10,2) DEFAULT 0.00 COMMENT '利润率',"
                + "`remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',"
                + "`owner_user_id` bigint(0) DEFAULT NULL COMMENT '负责人',"
                + "`create_user_id` bigint(0) DEFAULT NULL COMMENT '创建人',"
                + "`create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',"
                + "`update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',"
                + "`batch_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批次ID',"
                + "PRIMARY KEY (`order_id`) USING BTREE,"
                + "UNIQUE KEY `uk_order_number` (`order_number`) USING BTREE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单表' ROW_FORMAT=Dynamic");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `wk_crm_order_data` ("
                + "`id` int(0) NOT NULL AUTO_INCREMENT,"
                + "`field_id` int(0) DEFAULT NULL,"
                + "`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段名称',"
                + "`value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '字段值',"
                + "`create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',"
                + "`batch_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批次ID',"
                + "PRIMARY KEY (`id`) USING BTREE,"
                + "KEY `idx_order_data_batch_id` (`batch_id`) USING BTREE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单扩展字段表' ROW_FORMAT=Dynamic");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `wk_crm_order_relation` ("
                + "`id` int(0) NOT NULL AUTO_INCREMENT,"
                + "`order_id` int(0) NOT NULL COMMENT '订单ID',"
                + "`relation_type` int(0) NOT NULL COMMENT '关联模块类型',"
                + "`relation_id` int(0) NOT NULL COMMENT '关联模块ID',"
                + "`relation_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联名称快照',"
                + "`create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',"
                + "PRIMARY KEY (`id`) USING BTREE,"
                + "KEY `idx_order_relation_order_id` (`order_id`) USING BTREE,"
                + "KEY `idx_order_relation_type_id` (`relation_type`, `relation_id`) USING BTREE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单关联关系表' ROW_FORMAT=Dynamic");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `wk_crm_order_product` ("
                + "`r_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '明细ID',"
                + "`order_id` int(0) NOT NULL COMMENT '订单ID',"
                + "`product_id` int(0) DEFAULT NULL COMMENT '产品ID',"
                + "`product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '产品名称快照',"
                + "`price` decimal(10,2) DEFAULT 0.00 COMMENT '产品标准单价',"
                + "`sales_price` decimal(10,2) DEFAULT 0.00 COMMENT '报价单价',"
                + "`num` decimal(10,2) DEFAULT 0.00 COMMENT '数量',"
                + "`discount` decimal(10,2) DEFAULT 100.00 COMMENT '折扣',"
                + "`subtotal` decimal(10,2) DEFAULT 0.00 COMMENT '报价小计',"
                + "`purchase_price` decimal(10,2) DEFAULT 0.00 COMMENT '采购单价',"
                + "`purchase_cost` decimal(10,2) DEFAULT 0.00 COMMENT '采购成本',"
                + "`logistics_cost` decimal(10,2) DEFAULT 0.00 COMMENT '物流成本',"
                + "`profit_amount` decimal(10,2) DEFAULT 0.00 COMMENT '利润金额',"
                + "`profit_rate` decimal(10,2) DEFAULT 0.00 COMMENT '利润率',"
                + "`unit` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',"
                + "`remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '明细备注',"
                + "`sort` int(0) DEFAULT 0 COMMENT '排序',"
                + "PRIMARY KEY (`r_id`) USING BTREE,"
                + "KEY `idx_order_product_order_id` (`order_id`) USING BTREE,"
                + "KEY `idx_order_product_product_id` (`product_id`) USING BTREE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单报价利润明细表' ROW_FORMAT=Dynamic");
    }

    private void repairOrderTable() {
        if (!tableExists(ORDER_TABLE)) {
            return;
        }
        Map<String, String> columnSqlMap = new LinkedHashMap<>();
        columnSqlMap.put("logistics_number", "ALTER TABLE `wk_crm_order` ADD COLUMN `logistics_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流单号' AFTER `order_status`");
        columnSqlMap.put("exchange_rate", "ALTER TABLE `wk_crm_order` ADD COLUMN `exchange_rate` decimal(12,6) DEFAULT 1.000000 COMMENT '汇率换算' AFTER `order_status`");
        columnSqlMap.put("handling_fee_cost", "ALTER TABLE `wk_crm_order` ADD COLUMN `handling_fee_cost` decimal(10,2) DEFAULT 0.00 COMMENT '平手续成本' AFTER `logistics_cost`");
        columnSqlMap.put("consumable_cost", "ALTER TABLE `wk_crm_order` ADD COLUMN `consumable_cost` decimal(10,2) DEFAULT 0.00 COMMENT '耗材成本' AFTER `handling_fee_cost`");
        columnSqlMap.put("other_cost", "ALTER TABLE `wk_crm_order` ADD COLUMN `other_cost` decimal(10,2) DEFAULT 0.00 COMMENT '其他成本' AFTER `consumable_cost`");
        int repaired = 0;
        for (Map.Entry<String, String> entry : columnSqlMap.entrySet()) {
            if (columnExists(ORDER_TABLE, entry.getKey())) {
                continue;
            }
            jdbcTemplate.execute(entry.getValue());
            repaired++;
        }
        if (repaired > 0) {
            log.info("repaired {} missing order schema columns", repaired);
        }
    }

    private void repairOrderProductTable() {
        if (!tableExists(ORDER_PRODUCT_TABLE)) {
            return;
        }
        Map<String, String> columnSqlMap = new LinkedHashMap<>();
        columnSqlMap.put("product_name", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '产品名称快照' AFTER `product_id`");
        columnSqlMap.put("purchase_price", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `purchase_price` decimal(10,2) DEFAULT 0.00 COMMENT '采购单价' AFTER `subtotal`");
        columnSqlMap.put("purchase_cost", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `purchase_cost` decimal(10,2) DEFAULT 0.00 COMMENT '采购成本' AFTER `purchase_price`");
        columnSqlMap.put("logistics_cost", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `logistics_cost` decimal(10,2) DEFAULT 0.00 COMMENT '物流成本' AFTER `purchase_cost`");
        columnSqlMap.put("profit_amount", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `profit_amount` decimal(10,2) DEFAULT 0.00 COMMENT '利润金额' AFTER `logistics_cost`");
        columnSqlMap.put("profit_rate", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `profit_rate` decimal(10,2) DEFAULT 0.00 COMMENT '利润率' AFTER `profit_amount`");
        columnSqlMap.put("remark", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '明细备注' AFTER `unit`");
        columnSqlMap.put("sort", "ALTER TABLE `wk_crm_order_product` ADD COLUMN `sort` int(0) DEFAULT 0 COMMENT '排序' AFTER `remark`");
        int repaired = 0;
        for (Map.Entry<String, String> entry : columnSqlMap.entrySet()) {
            if (columnExists(ORDER_PRODUCT_TABLE, entry.getKey())) {
                continue;
            }
            jdbcTemplate.execute(entry.getValue());
            repaired++;
        }
        if (repaired > 0) {
            log.info("repaired {} missing order product schema columns", repaired);
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}
