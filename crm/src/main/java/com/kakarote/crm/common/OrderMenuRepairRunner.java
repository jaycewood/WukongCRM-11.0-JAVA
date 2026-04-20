package com.kakarote.crm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 为已部署环境补齐订单模块导入导出菜单权限。
 */
@Component
@Slf4j
public class OrderMenuRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public OrderMenuRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("wk_admin_menu") || !menuExists(943)) {
            return;
        }
        Map<Integer, String> menuSqlMap = new LinkedHashMap<>();
        menuSqlMap.put(949, "INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`) VALUES (949, 943, '导入', 'excelimport', '/crmOrder/downloadExcel,/crmOrder/uploadExcel', NULL, 3, 6, 1, NULL)");
        menuSqlMap.put(950, "INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`) VALUES (950, 943, '导出', 'excelexport', '/crmOrder/allExportExcel,/crmOrder/batchExportExcel', NULL, 3, 7, 1, NULL)");
        int repaired = 0;
        for (Map.Entry<Integer, String> entry : menuSqlMap.entrySet()) {
            if (menuExists(entry.getKey())) {
                continue;
            }
            jdbcTemplate.execute(entry.getValue());
            repaired++;
        }
        if (repaired > 0) {
            log.info("repaired {} missing order menu rows", repaired);
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

    private boolean menuExists(Integer menuId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM `wk_admin_menu` WHERE `menu_id` = ?",
                Integer.class,
                menuId
        );
        return count != null && count > 0;
    }
}
