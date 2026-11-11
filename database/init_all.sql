-- =============================================================================
-- init_all.sql
-- Script khởi tạo toàn bộ Database theo thứ tự phụ thuộc (Chạy với user SNEAKER_STORE)
-- =============================================================================
SET DEFINE OFF;
WHENEVER SQLERROR EXIT SQL.SQLCODE;

@@01_sequences.sql
@@02_tables_and_constraints.sql
@@03_indexes.sql
@@04_sample_data.sql

EXIT;

