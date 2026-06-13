CREATE SCHEMA IF NOT EXISTS angle;

SET search_path TO angle;

CREATE OR REPLACE FUNCTION angle.set_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    i INT;
    suffix TEXT;
    table_name TEXT;
BEGIN
    FOR i IN 0..63 LOOP
        suffix := lpad(i::text, 2, '0');
        table_name := 'short_url_mapping_' || suffix;

        EXECUTE format('
            CREATE TABLE IF NOT EXISTS angle.%I (
                short_code CHAR(8) NOT NULL,
                origin_url VARCHAR(2048) NOT NULL,
                origin_url_hash CHAR(32) NOT NULL,
                create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                expire_days INT DEFAULT NULL,
                access_count BIGINT NOT NULL DEFAULT 0,
                status SMALLINT NOT NULL DEFAULT 1,
                creator VARCHAR(50) DEFAULT NULL,
                PRIMARY KEY (short_code),
                CONSTRAINT %I UNIQUE (origin_url_hash)
            )',
            table_name,
            'uk_origin_url_hash_' || suffix
        );

        EXECUTE format(
            'CREATE INDEX IF NOT EXISTS %I ON angle.%I (status, create_time)',
            'idx_status_create_time_' || suffix,
            table_name
        );

        EXECUTE format(
            'CREATE INDEX IF NOT EXISTS %I ON angle.%I (expire_days)',
            'idx_expire_time_' || suffix,
            table_name
        );

        EXECUTE format(
            'CREATE INDEX IF NOT EXISTS %I ON angle.%I (creator, create_time)',
            'idx_creator_create_time_' || suffix,
            table_name
        );

        EXECUTE format(
            'COMMENT ON TABLE angle.%I IS %L',
            table_name,
            '短链接映射表_' || i
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.short_code IS %L',
            table_name,
            '短链编码，固定8位'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.origin_url IS %L',
            table_name,
            '原始URL'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.origin_url_hash IS %L',
            table_name,
            '原始URL的MD5哈希值'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.create_time IS %L',
            table_name,
            '创建时间'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.update_time IS %L',
            table_name,
            '更新时间'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.expire_days IS %L',
            table_name,
            '过期天数'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.access_count IS %L',
            table_name,
            '访问次数'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.status IS %L',
            table_name,
            '状态：1-正常，0-禁用，2-已过期'
        );

        EXECUTE format(
            'COMMENT ON COLUMN angle.%I.creator IS %L',
            table_name,
            '创建者'
        );

        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_set_update_time ON angle.%I',
            table_name
        );

        EXECUTE format('
            CREATE TRIGGER trg_set_update_time
            BEFORE UPDATE ON angle.%I
            FOR EACH ROW
            EXECUTE FUNCTION angle.set_update_time()',
            table_name
        );
    END LOOP;
END $$;
