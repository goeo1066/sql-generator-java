package com.github.goeo1066.sqlgenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PostgreSqlCreator {
    @Getter
    private final EntityInfo entityInfo;

    public static <T> PostgreSqlCreator fromClass(Class<T> tClass) {
        EntityInfo info = PostgreSqlUtils.getEntityInfo(tClass);
        return new PostgreSqlCreator(info);
    }

    public String insertOnConflictDoNothing(String pkTarget) {
        InsertOnConflictCreator creator = new InsertOnConflictCreator();
        if (pkTarget != null) {
            creator.pkTarget(pkTarget);
        }
        creator.doNothingOnConflict();
        return creator.sqlForNamedParameter();
    }

    public String insertOnConflictDoUpdate(String pkTarget) {
        InsertOnConflictCreator creator = new InsertOnConflictCreator();
        if (pkTarget != null) {
            creator.pkTarget(pkTarget);
        }
        creator.doUpdateOnConflict();
        return creator.sqlForNamedParameter();
    }

    public String selectPaged(SelectSpec selectSpec) {
        SelectCreator creator = new SelectCreator()
                .orderBy(selectSpec.getOrderBy())
                .where(selectSpec.getWhere())
                .paged();
        return creator.sqlForSelect();
    }

    public String selectTotal(SelectSpec selectSpec) {
        SelectCreator creator = new SelectCreator()
                .orderBy(selectSpec.getOrderBy())
                .where(selectSpec.getWhere())
                .notPaged();
        return creator.sqlForSelect();
    }

    public String countTotal(SelectSpec selectSpec) {
        SelectCreator creator = new SelectCreator()
                .where(selectSpec.getWhere())
                .notPaged();
        return creator.sqlForCountTotal();
    }

    public String deleteByPk(String pkTarget) {
        String resolvedPkTarget = pkTarget == null ? "default" : pkTarget;
        List<String> conditions = new ArrayList<>();
        for (ColumnInfo columnInfo : entityInfo.getColumnInfos()) {
            if (columnInfo.isPk(resolvedPkTarget)) {
                conditions.add("%s = :%s".formatted(columnInfo.getColumnName(), columnInfo.getFieldName()));
            }
        }
        return "DELETE FROM %s WHERE %s".formatted(entityInfo.getFullTableName(), String.join(" AND ", conditions));
    }

    public String deleteByWhere(String where) {
        String sql = "DELETE FROM %s WHERE 1 = 1\n".formatted(entityInfo.getFullTableName());
        if (Utils.isNotBlank(where)) {
            sql += where;
        }
        return sql;
    }

    public String updateByWhere(String setClause, String where) {
        String sql = "UPDATE %s SET\n%s\nWHERE 1 = 1\n".formatted(entityInfo.getFullTableName(), setClause);
        if (Utils.isNotBlank(where)) {
            sql += where;
        }
        return sql;
    }

    // SQL: Insert On Conflict
    private class InsertOnConflictCreator {
        private boolean updateOnConflict = true;
        private String pkTarget;

        private String getPkTarget() {
            return pkTarget == null ? "default" : pkTarget;
        }

        public InsertOnConflictCreator doNothingOnConflict() {
            updateOnConflict = false;
            return this;
        }

        public InsertOnConflictCreator doUpdateOnConflict() {
            updateOnConflict = true;
            return this;
        }

        public String sqlForNamedParameter() {
            String sqlTemplate = """
                    INSERT INTO %s (
                        %s
                    ) VALUES (
                        %s
                    ) ON CONFLICT (%s) DO %s
                        %s
                    """;

            String tableName = entityInfo.getFullTableName();
            String columnNames = columnList();
            String fieldNames = fieldList();
            String onConflictKeyNames = onConflictKeyNames();
            String onConflict = updateOnConflict ? "UPDATE SET" : "NOTHING";
            String updateList = updateOnConflict ? updateList() : "";

            String sql = sqlTemplate.formatted(
                    tableName,
                    columnNames,
                    fieldNames,
                    onConflictKeyNames,
                    onConflict,
                    updateList
            );
            return sql.trim();
        }

        private String columnList() {
            List<String> columnNames = new ArrayList<>(entityInfo.getColumnInfos().size());
            for (ColumnInfo columnInfo : entityInfo.getColumnInfos()) {
                columnNames.add(columnInfo.getColumnName());
            }
            return String.join(",\n", columnNames);
        }

        private String fieldList() {
            List<String> fieldNames = new ArrayList<>(entityInfo.getColumnInfos().size());
            for (ColumnInfo columnInfo : entityInfo.getColumnInfos()) {
                fieldNames.add(":%s".formatted(columnInfo.getFieldName()));
            }
            return String.join(",\n", fieldNames);
        }

        private String onConflictKeyNames() {
            List<String> columnNames = new ArrayList<>(entityInfo.getColumnInfos().size());
            for (ColumnInfo columnInfo : entityInfo.getColumnInfos()) {
                if (columnInfo.isPk(getPkTarget())) {
                    columnNames.add(columnInfo.getColumnName());
                }
            }
            return String.join(", ", columnNames);
        }

        private String updateList() {
            List<String> lineList = new ArrayList<>(entityInfo.getColumnInfos().size());
            for (ColumnInfo columnInfo : entityInfo.getColumnInfos()) {
                if (columnInfo.isNotOnUpdate()) {
                    continue;
                }
                if (columnInfo.isPk(getPkTarget())) {
                    continue;
                }
                lineList.add("%1$s = EXCLUDED.%1$s".formatted(columnInfo.getColumnName()));
            }
            return String.join(",\n", lineList);
        }

        public InsertOnConflictCreator pkTarget(String pkTarget) {
            this.pkTarget = pkTarget;
            return this;
        }
    }

    // SQL: Select
    private class SelectCreator {
        private static final String DEFAULT_ORDER_BY = "T.idx desc";
        private String rowNumName = "ROW_NUM";
        private String countName = "CNT";
        private String orderBy = DEFAULT_ORDER_BY;
        private String limitFieldName = "limit";
        private String offsetFieldName = "offset";
        private String where = "";
        private boolean isPaged = false;

        public SelectCreator rowNumName(String rowNumName) {
            this.rowNumName = rowNumName;
            return this;
        }

        public SelectCreator countName(String countName) {
            this.countName = countName;
            return this;
        }

        public SelectCreator orderBy(String orderBy) {
            if (Utils.isNotBlank(orderBy)) {
                this.orderBy = orderBy;
            } else {
                this.orderBy = DEFAULT_ORDER_BY;
            }
            return this;
        }

        public SelectCreator limitFieldName(String limitFieldName) {
            this.limitFieldName = limitFieldName;
            return this;
        }

        public SelectCreator offsetFieldName(String offsetFieldName) {
            this.offsetFieldName = offsetFieldName;
            return this;
        }

        public SelectCreator where(String where) {
            this.where = where;
            return this;
        }

        public SelectCreator paged() {
            this.isPaged = true;
            return this;
        }

        public SelectCreator notPaged() {
            this.isPaged = false;
            return this;
        }

        private String buildCoreSelectQuery() {
            String sql = """
                    SELECT  ROW_NUMBER() OVER(ORDER BY %s) AS %S
                         ,  T.*
                      FROM  %s T
                     WHERE  1 = 1
                    """;

            sql = sql.formatted(
                    orderBy,
                    rowNumName,
                    entityInfo.getFullTableName()
            );

            sql += where;

            return sql;
        }

        public String sqlForCountTotal() {
            String sql = """
                    SELECT  COUNT(*) AS %s
                      FROM  ( %s ) X
                    """;
            String coreSql = buildCoreSelectQuery();
            sql = sql.formatted(
                    countName, coreSql
            );
            return sql;
        }

        public String sqlForSelect() {
            String coreSql = buildCoreSelectQuery();
            String sql = """
                    SELECT  X.*
                      FROM  ( %s ) X
                     WHERE 1 = 1
                    """.formatted(coreSql);

            if (isPaged) {
                sql += " AND (X.%s > :%s AND X.%s <= (:%s + :%s) )".formatted(
                        rowNumName, offsetFieldName, rowNumName, offsetFieldName, limitFieldName
                );
            }

            return sql;
        }
    }
}
