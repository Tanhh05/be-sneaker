-- =============================================================================
-- 00_create_schema.sql
-- Tạo User/Schema SNEAKER_STORE trên Oracle Database (FREEPDB1 / XEPDB1 / ORCLPDB1)
-- Chạy với quyền SYSDBA
-- =============================================================================

ALTER SESSION SET CONTAINER = FREEPDB1;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM all_users WHERE username = 'SNEAKER_STORE';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP USER SNEAKER_STORE CASCADE';
    END IF;
END;
/

CREATE USER SNEAKER_STORE IDENTIFIED BY "SneakerStore123"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE, CREATE SYNONYM TO SNEAKER_STORE;
EXIT;

