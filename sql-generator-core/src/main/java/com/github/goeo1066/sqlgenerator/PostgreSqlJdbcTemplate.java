package com.github.goeo1066.sqlgenerator;

import com.github.goeo1066.sqlgenerator.core.SqlJdbcTemplate;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

public class PostgreSqlJdbcTemplate<T> implements SqlJdbcTemplate<T> {
    private final PostgreSqlCreator sqlCreator;
    @Getter
    private final NamedParameterJdbcTemplate jdbc;
    private final List<BeforeConvertCallback> beforeConvertCallbacks;

    @Getter
    private final RowMapper<T> rowMapper;

    public static <T> PostgreSqlJdbcTemplate<T> of(Class<T> tClass, NamedParameterJdbcTemplate jdbc, List<BeforeConvertCallback> beforeConvertCallbacks) {
        return new PostgreSqlJdbcTemplate<>(tClass, jdbc, beforeConvertCallbacks);
    }

    public PostgreSqlJdbcTemplate(Class<T> tClass, NamedParameterJdbcTemplate jdbc, List<BeforeConvertCallback> beforeConvertCallbacks) {
        this.sqlCreator = PostgreSqlCreator.fromClass(tClass);
        this.jdbc = jdbc;
        this.rowMapper = BeanPropertyRowMapper.newInstance(tClass);
        this.beforeConvertCallbacks = beforeConvertCallbacks == null ? List.of() : beforeConvertCallbacks;
    }

    @Override
    public void insertOrUpdate(List<T> list, String pkTarget) {
        insertOnConflictDoUpdate(list, pkTarget);
    }

    @Override
    public void insertOrUpdate(List<T> list) {
        insertOnConflictDoUpdate(list);
    }

    @Override
    public void insertOrIgnore(List<T> list, String pkTarget) {
        insertOnConflictDoNothing(list, pkTarget);
    }

    @Override
    public void insertOrIgnore(List<T> list) {
        insertOnConflictDoNothing(list);
    }

    public void insertOnConflictDoUpdate(List<T> list, String pkTarget) {
        String sql = sqlCreator.insertOnConflictDoUpdate(pkTarget);
        batchUpdate(list, sql);
    }

    public void insertOnConflictDoUpdate(List<T> list) {
        String sql = sqlCreator.insertOnConflictDoUpdate(null);
        batchUpdate(list, sql);
    }

    public void insertOnConflictDoNothing(List<T> list, String pkTarget) {
        String sql = sqlCreator.insertOnConflictDoNothing(pkTarget);
        batchUpdate(list, sql);
    }

    public void insertOnConflictDoNothing(List<T> list) {
        String sql = sqlCreator.insertOnConflictDoNothing(null);
        batchUpdate(list, sql);
    }

    @Override
    public void updateSet(String namedSqlTemplate, SqlParameterSource sqlParameterSource) {
        EntityInfo entityInfo = sqlCreator.getEntityInfo();
        String fullName = entityInfo.getFullTableName();
        String header = "UPDATE %s SET\n".formatted(fullName);
        namedSqlTemplate = header + namedSqlTemplate;

        jdbc.batchUpdate(namedSqlTemplate, new SqlParameterSource[]{sqlParameterSource});
    }

    @Override
    public List<T> selectPaged(SelectSpec selectSpec) {
        String sql = sqlCreator.selectPaged(selectSpec);
        return jdbc.query(sql, selectSpec.getSqlParameterSource(), getRowMapper());
    }

    @Override
    public List<T> selectTotal(SelectSpec selectSpec) {
        String sql = sqlCreator.selectTotal(selectSpec);
        return jdbc.query(sql, selectSpec.getSqlParameterSource(), getRowMapper());
    }

    @Override
    public int deleteByPk(T entity, String pkTarget) {
        String sql = sqlCreator.deleteByPk(pkTarget);
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(entity);
        return jdbc.update(sql, sqlParameterSource);
    }

    @Override
    public int deleteByPk(T entity) {
        return deleteByPk(entity, null);
    }

    @Override
    public int deleteByWhere(String where, SqlParameterSource sqlParameterSource) {
        String sql = sqlCreator.deleteByWhere(where);
        return jdbc.update(sql, sqlParameterSource);
    }

    @Override
    public int deleteByWhere(String where) {
        String sql = sqlCreator.deleteByWhere(where);
        return jdbc.update(sql, new MapSqlParameterSource());
    }

    @Override
    public int updateByWhere(String setClause, String where, SqlParameterSource sqlParameterSource) {
        String sql = sqlCreator.updateByWhere(setClause, where);
        return jdbc.update(sql, sqlParameterSource);
    }

    @Override
    public long countTotal(SelectSpec selectSpec) {
        String sql = sqlCreator.countTotal(selectSpec);
        Long cnt = jdbc.queryForObject(sql, selectSpec.getSqlParameterSource(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    private void batchUpdate(List<T> list, String sql) {
        SqlParameterSource[] sqlParameterSources = new SqlParameterSource[list.size()];
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            t = applyBeforeCallbacks(t);

            SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(t);
            sqlParameterSources[i] = sqlParameterSource;
        }
        jdbc.batchUpdate(sql, sqlParameterSources);
    }

    private T applyBeforeCallbacks(T t) {
        Object o = t;
        for (BeforeConvertCallback beforeConvertCallback : beforeConvertCallbacks) {
            var r = beforeConvertCallback.onBeforeConvert(o);
            if (r == null) {
                continue;
            }
            o = r;
        }
        return (T) o;
    }
}
