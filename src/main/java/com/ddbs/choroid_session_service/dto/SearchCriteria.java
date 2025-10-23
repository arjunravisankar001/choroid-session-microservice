package com.ddbs.choroid_session_service.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SearchCriteria {

    private final StringBuilder sql = new StringBuilder("SELECT * FROM sessions");
    private final StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM sessions");
    private final StringBuilder whereClause = new StringBuilder("WHERE true");
    private final List<Object> filterParams = new ArrayList<>();
    private String orderBy = "start"; //default sort column
    private String sortDirection = "ASC"; //default sort direction
    @Getter
    private int limit = 20; //default limit
    @Getter
    private int offset = 0; //default offset

    public void addCondition(String condition, Object value)
    {
        whereClause.append(" AND ").append(condition);
        filterParams.add(value);
    }

    public void setSorting(String orderBy, String sortDirection)
    {
        this.orderBy = orderBy;
        this.sortDirection = sortDirection;
    }

    public void setPagination(int page, int size)
    {
        this.offset = page * size;
        this.limit = size;
    }

    public String getCountSql()
    {
        countSql.append(" ").append(whereClause);
        log.debug(countSql.toString());
        return countSql.toString();
    }

    public Object[] getFilterParams()
    {
        log.debug(filterParams.toString());
        return filterParams.toArray();
    }

    public String getSql()
    {
        sql.append(" ").append(whereClause);
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(sortDirection);
        sql.append(" LIMIT ? OFFSET ?");
        log.debug(sql.toString());
        return sql.toString();
    }

    public Object[] getFinalParams()
    {
        List<Object> finalParams = new ArrayList<>(filterParams);
        finalParams.add(limit);
        finalParams.add(offset);
        log.debug(finalParams.toString());
        return finalParams.toArray();
    }

}
