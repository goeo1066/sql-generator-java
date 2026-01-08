package com.example.demo.database.config;

import com.example.demo.database.PostgreSqlJdbcTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class PostgreSqlJdbcTemplateFactoryBean<T> implements FactoryBean<PostgreSqlJdbcTemplate<T>> {
    private final Class<T> type;
    private final NamedParameterJdbcTemplate jdbc;
    private final List<BeforeConvertCallback> beforeConvertCallbacks;

    public PostgreSqlJdbcTemplateFactoryBean(Class<T> type, NamedParameterJdbcTemplate jdbc, List<BeforeConvertCallback> beforeConvertCallbacks) {
        this.type = type;
        this.jdbc = jdbc;
        this.beforeConvertCallbacks = beforeConvertCallbacks;
    }

    @Override
    public @Nullable PostgreSqlJdbcTemplate<T> getObject() throws Exception {
        return PostgreSqlJdbcTemplate.of(type, jdbc, beforeConvertCallbacks);
    }

    @Override
    public @Nullable Class<?> getObjectType() {
        return null;
    }
}
