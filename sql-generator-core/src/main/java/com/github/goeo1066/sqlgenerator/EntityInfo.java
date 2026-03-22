package com.github.goeo1066.sqlgenerator;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record EntityInfo(
        String schemaName,
        String tableName,
        List<ColumnInfo> columnInfos
) {
    public String getFullTableName() {
        if (Util.Strings.isBlank(schemaName)) {
            return tableName;
        } else {
            return "%s.%s".formatted(schemaName, tableName);
        }
    }
}
