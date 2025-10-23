package com.ddbs.choroid_session_service.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UpdateFields {

    private final StringBuilder sql = new StringBuilder("UPDATE sessions SET ");
    private final List<Object> updateParams = new ArrayList<>();
    private boolean firstField = true;

    public void addField(String fieldName, Object value)
    {
        if (!firstField) {
            sql.append(", ");
        }
        if (!fieldName.equals("tags")) {
            sql.append(fieldName).append(" = ?");
        }
        else {
            // Use the JSONB set operator for tags
            sql.append(fieldName).append(" = ?::jsonb");
        }
        updateParams.add(value);
        firstField = false;
    }

    public String getSql()
    {
        if (firstField) {
            throw new IllegalStateException("No fields to update");
        }
        sql.append(" WHERE id = ?");
        log.debug(sql.toString());
        return sql.toString();
    }

    public Object[] getUpdateParams(Object id)
    {
        if (firstField) {
            throw new IllegalStateException("No fields to update");
        }
        updateParams.add(id);
        log.debug(updateParams.toString());
        return updateParams.toArray();
    }
}
