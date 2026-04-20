-- Repair missing role-field metadata for CRM order module (label = 19).
-- This is safe to run repeatedly.

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT
  r.`role_id`,
  19,
  f.`field_id`,
  f.`field_name`,
  f.`name`,
  3,
  CASE WHEN f.`field_name` = 'order_number' THEN 3 ELSE 1 END,
  0,
  f.`field_type`
FROM `wk_admin_role` r
JOIN `wk_crm_field` f ON f.`label` = 19
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = f.`field_name`
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

UPDATE `wk_crm_role_field` rf
JOIN `wk_crm_field` f
  ON rf.`field_id` = f.`field_id`
SET
  rf.`field_name` = f.`field_name`,
  rf.`name` = f.`name`,
  rf.`auth_level` = IFNULL(rf.`auth_level`, 3),
  rf.`operate_type` = CASE WHEN f.`field_name` = 'order_number' THEN 3 ELSE 1 END,
  rf.`mask_type` = IFNULL(rf.`mask_type`, 0),
  rf.`field_type` = IFNULL(rf.`field_type`, f.`field_type`)
WHERE rf.`label` = 19
  AND f.`label` = 19;

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT r.`role_id`, 19, NULL, 'create_user_name', '创建人', 2, 2, 0, 1
FROM `wk_admin_role` r
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = 'create_user_name'
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT r.`role_id`, 19, NULL, 'create_time', '创建时间', 2, 2, 0, 1
FROM `wk_admin_role` r
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = 'create_time'
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT r.`role_id`, 19, NULL, 'update_time', '更新时间', 2, 2, 0, 1
FROM `wk_admin_role` r
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = 'update_time'
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT r.`role_id`, 19, NULL, 'owner_user_name', '负责人', 2, 4, 0, 1
FROM `wk_admin_role` r
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = 'owner_user_name'
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

INSERT INTO `wk_crm_role_field`
(`role_id`, `label`, `field_id`, `field_name`, `name`, `auth_level`, `operate_type`, `mask_type`, `field_type`)
SELECT r.`role_id`, 19, NULL, 'owner_dept_name', '所属部门', 2, 4, 0, 1
FROM `wk_admin_role` r
LEFT JOIN `wk_crm_role_field` rf
  ON rf.`role_id` = r.`role_id`
 AND rf.`label` = 19
 AND rf.`field_name` = 'owner_dept_name'
WHERE r.`role_type` = 2
  AND rf.`id` IS NULL;

UPDATE `wk_crm_role_field`
SET `name` = '创建人', `auth_level` = 2, `operate_type` = 2, `mask_type` = 0, `field_type` = 1
WHERE `label` = 19 AND `field_name` = 'create_user_name';

UPDATE `wk_crm_role_field`
SET `name` = '创建时间', `auth_level` = 2, `operate_type` = 2, `mask_type` = 0, `field_type` = 1
WHERE `label` = 19 AND `field_name` = 'create_time';

UPDATE `wk_crm_role_field`
SET `name` = '更新时间', `auth_level` = 2, `operate_type` = 2, `mask_type` = 0, `field_type` = 1
WHERE `label` = 19 AND `field_name` = 'update_time';

UPDATE `wk_crm_role_field`
SET `name` = '负责人', `auth_level` = 2, `operate_type` = 4, `mask_type` = 0, `field_type` = 1
WHERE `label` = 19 AND `field_name` = 'owner_user_name';

UPDATE `wk_crm_role_field`
SET `name` = '所属部门', `auth_level` = 2, `operate_type` = 4, `mask_type` = 0, `field_type` = 1
WHERE `label` = 19 AND `field_name` = 'owner_dept_name';
