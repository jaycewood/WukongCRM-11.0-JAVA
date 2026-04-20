SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'wk_crm_order'
          AND column_name = 'exchange_rate'
    ),
    'SELECT 1',
    'ALTER TABLE `wk_crm_order` ADD COLUMN `exchange_rate` decimal(12,6) DEFAULT 1.000000 COMMENT ''汇率换算'' AFTER `order_status`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'wk_crm_order'
          AND column_name = 'handling_fee_cost'
    ),
    'SELECT 1',
    'ALTER TABLE `wk_crm_order` ADD COLUMN `handling_fee_cost` decimal(10,2) DEFAULT 0.00 COMMENT ''平手续成本'' AFTER `logistics_cost`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'wk_crm_order'
          AND column_name = 'consumable_cost'
    ),
    'SELECT 1',
    'ALTER TABLE `wk_crm_order` ADD COLUMN `consumable_cost` decimal(10,2) DEFAULT 0.00 COMMENT ''耗材成本'' AFTER `handling_fee_cost`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'wk_crm_order'
          AND column_name = 'other_cost'
    ),
    'SELECT 1',
    'ALTER TABLE `wk_crm_order` ADD COLUMN `other_cost` decimal(10,2) DEFAULT 0.00 COMMENT ''其他成本'' AFTER `consumable_cost`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
