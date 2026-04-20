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

    private final JdbcTemplate jdbcTemplate;

    public OrderSchemaRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists(ORDER_TABLE)) {
            return;
        }
        Map<String, String> columnSqlMap = new LinkedHashMap<>();
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
