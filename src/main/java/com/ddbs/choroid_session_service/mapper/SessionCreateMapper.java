package com.ddbs.choroid_session_service.mapper;

import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.dto.CreateSessionRequest;

import java.util.UUID;

public class SessionCreateMapper {

    //CreateSessionRequest to Session
    public static Session fromCreateRequest(CreateSessionRequest request)
    {
        return new Session(
                UUID.randomUUID(), // Generate a new UUID for the session
                request.getCreatorId().trim(),
                request.getTitle().trim(),
                request.getStart(),
                request.getDuration(),
                request.getTags(),
                request.getMeetingLink().trim(),
                request.getResourcesLink().trim()
        );
    }
}
