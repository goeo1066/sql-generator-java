package com.github.goeo1066.sqlgenerator.core;

import com.github.goeo1066.sqlgenerator.EntityInfo;
import com.github.goeo1066.sqlgenerator.SelectSpec;

public interface SqlCreator {

    EntityInfo getEntityInfo();

    // INSERT (upsert)
    String insertOrUpdate(String pkTarget);

    String insertOrIgnore(String pkTarget);

    // SELECT
    String selectPaged(SelectSpec selectSpec);

    String selectTotal(SelectSpec selectSpec);

    String countTotal(SelectSpec selectSpec);

    // UPDATE
    String updateByWhere(String setClause, String where);

    // DELETE
    String deleteByPk(String pkTarget);

    String deleteByWhere(String where);
}
