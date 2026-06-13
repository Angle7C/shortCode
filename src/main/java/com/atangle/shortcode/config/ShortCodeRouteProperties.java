package com.atangle.shortcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "short-code.route")
public class ShortCodeRouteProperties {

    private int schemaCount = 16;

    private int tableCountPerSchema = 64;

    private String schemaPrefix = "code_";

    private String tablePrefix = "short_url_mapping_";

    public int getSchemaCount() {
        return schemaCount;
    }

    public void setSchemaCount(int schemaCount) {
        this.schemaCount = schemaCount;
    }

    public int getTableCountPerSchema() {
        return tableCountPerSchema;
    }

    public void setTableCountPerSchema(int tableCountPerSchema) {
        this.tableCountPerSchema = tableCountPerSchema;
    }

    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }
}
