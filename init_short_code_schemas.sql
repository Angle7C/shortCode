CREATE OR REPLACE FUNCTION public.set_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    schema_idx INT;
    table_idx INT;
    schema_name TEXT;
    suffix TEXT;
    table_name TEXT;
BEGIN
    FOR schema_idx IN 0..15 LOOP
        schema_name := 'code_' || lpad(schema_idx::text, 2, '0');
        EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', schema_name);

        FOR table_idx IN 0..63 LOOP
            suffix := lpad(table_idx::text, 2, '0');
            table_name := 'short_url_mapping_' || suffix;

            EXECUTE format('
                CREATE TABLE IF NOT EXISTS %I.%I (
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
                schema_name,
                table_name,
                'uk_origin_url_hash_' || schema_idx || '_' || suffix
            );

            EXECUTE format(
                'CREATE INDEX IF NOT EXISTS %I ON %I.%I (status, create_time)',
                'idx_status_create_time_' || schema_idx || '_' || suffix,
                schema_name,
                table_name
            );

            EXECUTE format(
                'CREATE INDEX IF NOT EXISTS %I ON %I.%I (expire_days)',
                'idx_expire_time_' || schema_idx || '_' || suffix,
                schema_name,
                table_name
            );

            EXECUTE format(
                'CREATE INDEX IF NOT EXISTS %I ON %I.%I (creator, create_time)',
                'idx_creator_create_time_' || schema_idx || '_' || suffix,
                schema_name,
                table_name
            );

            EXECUTE format(
                'COMMENT ON TABLE %I.%I IS %L',
                schema_name,
                table_name,
                schema_name || '.短链接映射表_' || table_idx
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.short_code IS %L',
                schema_name,
                table_name,
                '短链编码，固定8位'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.origin_url IS %L',
                schema_name,
                table_name,
                '原始URL'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.origin_url_hash IS %L',
                schema_name,
                table_name,
                '原始URL的MD5哈希值'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.create_time IS %L',
                schema_name,
                table_name,
                '创建时间'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.update_time IS %L',
                schema_name,
                table_name,
                '更新时间'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.expire_days IS %L',
                schema_name,
                table_name,
                '过期天数'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.access_count IS %L',
                schema_name,
                table_name,
                '访问次数'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.status IS %L',
                schema_name,
                table_name,
                '状态：1-正常，0-禁用，2-已过期'
            );

            EXECUTE format(
                'COMMENT ON COLUMN %I.%I.creator IS %L',
                schema_name,
                table_name,
                '创建者'
            );

            EXECUTE format(
                'DROP TRIGGER IF EXISTS trg_set_update_time ON %I.%I',
                schema_name,
                table_name
            );

            EXECUTE format('
                CREATE TRIGGER trg_set_update_time
                BEFORE UPDATE ON %I.%I
                FOR EACH ROW
                EXECUTE FUNCTION public.set_update_time()',
                schema_name,
                table_name
            );
        END LOOP;
    END LOOP;
END $$;
