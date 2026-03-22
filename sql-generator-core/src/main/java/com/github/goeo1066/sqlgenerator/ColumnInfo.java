package com.github.goeo1066.sqlgenerator;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.Set;

@Data
public class ColumnInfo {
    private String fieldName;
    private String columnName;
    private boolean bool;
    private Set<String> pkTargets;
    private Method method;
    private boolean notOnUpdate;

    public String getGetterName() {
        String propertyName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String prefix = isBool() ? "is" : "get";
        return prefix + propertyName;
    }

    public boolean isPk(String pkTarget) {
        return pkTargets != null && pkTargets.contains(pkTarget);
    }
}
