-- =====================================================================
-- Fix script: add missing update_time column to tables that extend BaseEntity
--
-- Background:
--   BaseEntity declares createTime / updateTime (FieldFill.INSERT_UPDATE)
--   plus the logical-delete field `deleted`. Some CREATE TABLE statements
--   omitted `update_time`, so MyBatis-Plus generated SQL containing that
--   column and failed with:
--       Unknown column 'update_time' in 'field list'
--
-- Affected tables (entity extends BaseEntity but table lacked update_time):
--   candidate_favorite, company_member, content_audit, project_asset,
--   project_member, review_appeal, review_reply, sys_dict, verify_record
--
-- Not touched: company_view_log and track_event -- their entities do NOT
--   extend BaseEntity, so they legitimately have no update_time.
--
-- Usage: mysql -uroot -p xiaozhan < sql/fix_update_time.sql
-- Idempotent: tables that already have the column are skipped.
-- =====================================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS `__add_update_time`;
DELIMITER $$
CREATE PROCEDURE `__add_update_time`()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE tname VARCHAR(64);
  DECLARE cur CURSOR FOR
    SELECT t.TABLE_NAME
    FROM information_schema.TABLES t
    WHERE t.TABLE_SCHEMA = DATABASE()
      AND t.TABLE_NAME IN ('candidate_favorite','company_member','content_audit','project_asset',
                           'project_member','review_appeal','review_reply','sys_dict','verify_record')
      AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS c
        WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA
          AND c.TABLE_NAME = t.TABLE_NAME
          AND c.COLUMN_NAME = 'update_time'
      );
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO tname;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;
    SET @ddl = CONCAT('ALTER TABLE `', tname, '` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SELECT CONCAT('added update_time -> ', tname) AS result;
  END LOOP;
  CLOSE cur;
END $$
DELIMITER ;

CALL `__add_update_time`();

DROP PROCEDURE IF EXISTS `__add_update_time`;

-- Verification: must return 0 rows
SELECT t.TABLE_NAME AS missing_update_time
FROM information_schema.TABLES t
WHERE t.TABLE_SCHEMA = DATABASE()
  AND t.TABLE_NAME IN ('candidate_favorite','company_member','content_audit','project_asset',
                       'project_member','review_appeal','review_reply','sys_dict','verify_record')
  AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS c
    WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA
      AND c.TABLE_NAME = t.TABLE_NAME
      AND c.COLUMN_NAME = 'update_time'
  );
