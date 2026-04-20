INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 949, 943, '导入', 'excelimport', '/crmOrder/downloadExcel,/crmOrder/uploadExcel', NULL, 3, 6, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 949);

INSERT INTO `wk_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `realm_url`, `realm_module`, `menu_type`, `sort`, `status`, `remarks`)
SELECT 950, 943, '导出', 'excelexport', '/crmOrder/allExportExcel,/crmOrder/batchExportExcel', NULL, 3, 7, 1, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wk_admin_menu` WHERE `menu_id` = 950);
