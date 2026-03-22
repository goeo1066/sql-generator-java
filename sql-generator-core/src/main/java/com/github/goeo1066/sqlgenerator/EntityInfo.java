package com.github.goeo1066.sqlgenerator;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Builder
@Data
public class EntityInfo {
    private final String schemaName;
    private final String tableName;
    private final List<ColumnInfo> columnInfos;

    public EntityInfo(
            String schemaName,
            String tableName,
            List<ColumnInfo> columnInfos
    ) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnInfos = columnInfos;
    }

    public String getFullTableName() {
        if (Utils.isBlank(schemaName)) {
            return tableName;
        } else {
            return "%s.%s".formatted(schemaName, tableName);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EntityInfo) obj;
        return Objects.equals(this.schemaName, that.schemaName) &&
                Objects.equals(this.tableName, that.tableName) &&
                Objects.equals(this.columnInfos, that.columnInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaName, tableName, columnInfos);
    }

    @Override
    public String toString() {
        return "EntityInfo[" +
                "schemaName=" + schemaName + ", " +
                "tableName=" + tableName + ", " +
                "columnInfos=" + columnInfos + ']';
    }
}
