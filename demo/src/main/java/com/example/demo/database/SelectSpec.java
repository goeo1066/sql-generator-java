package com.example.demo.database;

import lombok.Data;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;
import java.util.function.Supplier;

@Data
public class SelectSpec {
    private static final Supplier<SelectSpec> DEFAULT_SUPPLIER = () -> new SelectSpec("", "", new MapSqlParameterSource());
    private String orderBy;
    private String where;
    private SqlParameterSource sqlParameterSource;

    public SelectSpec(
            String orderBy,
            String where,
            SqlParameterSource sqlParameterSource
    ) {
        this.orderBy = orderBy;
        this.where = where;
        this.sqlParameterSource = sqlParameterSource;
    }

    public static SelectSpec ofDefault() {
        return DEFAULT_SUPPLIER.get();
    }

    public <T> void setSqlParameterSource(T object) {
        this.sqlParameterSource = new BeanPropertySqlParameterSource(object);
    }

    public void setSqlParameterSource(SqlParameterSource sqlParameterSource) {
        this.sqlParameterSource = sqlParameterSource;
    }

    public void setSqlParameterSource(Map<String, ?> map) {
        this.sqlParameterSource = new MapSqlParameterSource(map);
    }

    public SqlParameterSource getSqlParameterSource() {
        if (sqlParameterSource == null) {
            sqlParameterSource = new MapSqlParameterSource();
        }
        return sqlParameterSource;
    }
}
