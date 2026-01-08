package com.example.demo.database;

import com.example.demo.Util;
import com.example.demo.database.annotations.NoUpdate;
import com.example.demo.database.annotations.Pk;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class PostgreSqlUtils {

    public static <T> EntityInfo getEntityInfo(Class<T> tClass) {
        EntityInfo.EntityInfoBuilder entityInfoBuilder = EntityInfo.builder();
        Table table = tClass.getAnnotation(Table.class);

        entityInfoBuilder.schemaName(table.schema());
        entityInfoBuilder.tableName(table.name());
        entityInfoBuilder.columnInfos(getColumnInfoList(tClass));

        return entityInfoBuilder.build();
    }

    private static <T> List<ColumnInfo> getColumnInfoList(Class<T> tClass) {
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        getColumnInfoListRecursive(tClass, columnInfoList);
        return filterMatchedProperties(tClass, columnInfoList);
    }

    private static <T> void getColumnInfoListRecursive(Class<T> tClass, List<ColumnInfo> result) {
        if (tClass == Object.class) {
            return;
        }
        result.addAll(getColumnListFromFields(tClass.getDeclaredFields()));
        getColumnInfoListRecursive(tClass.getSuperclass(), result);
    }

    private static <T> List<ColumnInfo> filterMatchedProperties(Class<T> tClass, List<ColumnInfo> columnInfoList) {
        boolean isPersistable = tClass.isInstance(Persistable.class);
        List<ColumnInfo> columnInfos = new ArrayList<>(columnInfoList.size());
        for (ColumnInfo columnInfo : columnInfoList) {
            String getterName = columnInfo.getGetterName();
            if (isPersistable && "isNew".equals(getterName)) {
                // Persistable이 구현된 경우 isNew는 대상 필드에서 제외한다.
                continue;
            }

            try {
                Method method = tClass.getMethod(getterName);
                columnInfo.setMethod(method);
                columnInfos.add(columnInfo);
            } catch (NoSuchMethodException e) {
                continue;
            }
        }
        return columnInfos;
    }

    private static List<ColumnInfo> getColumnListFromFields(Field[] declaredFields) {
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            ColumnInfo columnInfo = getColumnInfoFromField(declaredField);
            if (columnInfo == null) {
                continue;
            }
            columnInfoList.add(columnInfo);
        }
        return columnInfoList;
    }

    private static ColumnInfo getColumnInfoFromField(Field declaredField) {
        Transient tr = declaredField.getAnnotation(Transient.class);
        if (tr != null) {
            return null;
        }


        if (declaredField.accessFlags().contains(AccessFlag.STATIC)) {
            return null;
        }

        Column column = declaredField.getAnnotation(Column.class);
        Id id = declaredField.getAnnotation(Id.class);
        Pk pk = declaredField.getAnnotation(Pk.class);
        NoUpdate noUpdate = declaredField.getAnnotation(NoUpdate.class);

        Set<String> pkTargets = getPkTargetsFrom(id, pk);

        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setPkTargets(pkTargets);
        columnInfo.setFieldName(declaredField.getName());
        columnInfo.setNotOnUpdate(noUpdate != null);

        // Column Name
        String columnName;
        if (column != null && Util.Strings.isNotBlank(column.value())) {
            columnName = column.value();
        } else {
            columnName = fieldNameToColumnName(declaredField.getName());
        }
        columnInfo.setColumnName(columnName);

        // Is Bool
        boolean isBoolean = isBoolean(declaredField.getType());
        columnInfo.setBool(isBoolean);
        return columnInfo;
    }

    private static Set<String> getPkTargetsFrom(Id id, Pk pk) {
        Set<String> pkTargets = new HashSet<>();
        if (id != null) {
            pkTargets.add("default");
        }

        if (pk != null) {
            if (pk.pkTargets() == null || pk.pkTargets().length == 0) {
                pkTargets.add("default");
            } else {
                pkTargets.addAll(Arrays.asList(pk.pkTargets()));
            }
        }

        if (pkTargets.isEmpty()) {
            return null;
        }
        return pkTargets;
    }

    private static boolean isBoolean(Class clazz) {
        switch (clazz.getCanonicalName()) {
            case "boolean", "java.lang.Boolean" -> {
                return true;
            }
        }
        return false;
    }

    private static String fieldNameToColumnName(String fieldName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c) && i != 0) {
                builder.append('_');
            }
            builder.append(c);
        }
        return builder.toString().toUpperCase();
    }
}
