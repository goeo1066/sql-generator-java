package com.github.goeo1066.sqlgenerator.core;

import com.github.goeo1066.sqlgenerator.SelectSpec;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

public interface SqlJdbcTemplate<T> {

    NamedParameterJdbcTemplate getJdbc();

    RowMapper<T> getRowMapper();

    // INSERT (upsert)
    void insertOrUpdate(List<T> list, String pkTarget);

    void insertOrUpdate(List<T> list);

    void insertOrIgnore(List<T> list, String pkTarget);

    void insertOrIgnore(List<T> list);

    // SELECT
    List<T> selectPaged(SelectSpec selectSpec);

    List<T> selectTotal(SelectSpec selectSpec);

    long countTotal(SelectSpec selectSpec);

    // UPDATE
    void updateSet(String namedSqlTemplate, SqlParameterSource sqlParameterSource);

    int updateByWhere(String setClause, String where, SqlParameterSource sqlParameterSource);

    // DELETE
    int deleteByPk(T entity, String pkTarget);

    int deleteByPk(T entity);

    int deleteByWhere(String where, SqlParameterSource sqlParameterSource);

    int deleteByWhere(String where);
}
