package com.ddbs.choroid_session_service.mapper;

import com.ddbs.choroid_session_service.dto.UpdateFields;
import com.ddbs.choroid_session_service.dto.UpdateSessionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;

public class SessionUpdateMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UpdateFields toUpdateFields(UpdateSessionRequest request)
    {
        UpdateFields updateFields = new UpdateFields();
        //Apply only non-null fields
        if (request.getTitle() != null) {
            updateFields.addField("title", request.getTitle().trim());
        }
        if (request.getStart() != null) {
            updateFields.addField("start", Timestamp.valueOf(request.getStart()));
        }
        if (request.getDuration() != null) {
            updateFields.addField("duration", request.getDuration());
        }
        if (request.getTags() != null) {
            try {
                String jsonTags = objectMapper.writeValueAsString(request.getTags());
                updateFields.addField("tags", jsonTags);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid tag list format", e);
            }
        }
        if (request.getMeetingLink() != null) {
            updateFields.addField("meeting_link", request.getMeetingLink().trim());
        }
        if (request.getResourcesLink() != null) {
            updateFields.addField("resources_link", request.getResourcesLink().trim());
        }
        return updateFields;
    }
}
