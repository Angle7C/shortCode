package com.atangle.shortcode.routing;

public record RouteTarget(
        int schemaIndex,
        int tableIndex,
        String schemaName,
        String tableName
) {

    public String qualifiedTableName() {
        return schemaName + "." + tableName;
    }
}
