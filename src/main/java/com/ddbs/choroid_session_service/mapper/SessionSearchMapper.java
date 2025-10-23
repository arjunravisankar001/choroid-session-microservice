package com.ddbs.choroid_session_service.mapper;

import com.ddbs.choroid_session_service.dto.SearchCriteria;
import com.ddbs.choroid_session_service.dto.SearchSessionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;

public class SessionSearchMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SearchCriteria toSearchCriteria(SearchSessionRequest request)
    {
        SearchCriteria criteria = new SearchCriteria();
        // Apply filters based on non-null request fields
        if (request.getCreatorId() != null) {
            criteria.addCondition("creator_id = ?", request.getCreatorId().trim());
        }
        if (request.getTitleContains() != null) {
            criteria.addCondition("title ILIKE ?", "%" + request.getTitleContains().trim() + "%");
        }
        if (request.getStartAfter() != null) {
            criteria.addCondition("start >= ?", Timestamp.valueOf(request.getStartAfter()));
        }
        if (request.getStartBefore() != null) {
            criteria.addCondition("start <= ?", Timestamp.valueOf(request.getStartBefore()));
        }
        if (request.getMinDuration() != null) {
            criteria.addCondition("duration >= ?", request.getMinDuration());
        }
        if (request.getMaxDuration() != null) {
            criteria.addCondition("duration <= ?", request.getMaxDuration());
        }
        if (request.getTagsInclude() != null) {
            try {
                String jsonTags = objectMapper.writeValueAsString(request.getTagsInclude());
                criteria.addCondition("tags @> ?::jsonb", jsonTags);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid tag list format", e);
            }
        }

        //Sorting
        String orderBy = (request.getSortBy() != null) ? request.getSortBy() : "start";
        String sortDirection = (request.getSortOrder() != null && request.getSortOrder().equalsIgnoreCase("desc")) ? "DESC" : "ASC";
        criteria.setSorting(orderBy, sortDirection);

        //Pagination
        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 20;
        criteria.setPagination(page, size);

        return criteria;
    }

}
