package com.ddbs.choroid_session_service.service;

import com.ddbs.choroid_session_service.dto.*;
import com.ddbs.choroid_session_service.mapper.SessionCreateMapper;
import com.ddbs.choroid_session_service.mapper.SessionSearchMapper;
import com.ddbs.choroid_session_service.mapper.SessionUpdateMapper;
import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    //GET choroid/sessions/{id}
    public Session getSessionById(UUID id) {
        return sessionRepository.findById(id);
    }

    //GET choroid/sessions/tags
    public List<String> getTags()
    {
        return new ArrayList<>(sessionRepository.findUniqueTags());
    }

    //POST choroid/sessions
    public Session createSession(CreateSessionRequest request) {
        Session session = SessionCreateMapper.fromCreateRequest(request);
        return sessionRepository.save(session);
    }

    //POST choroid/sessions/search
    public PageResponse<Session> searchSessions(SearchSessionRequest request)
    {
        SearchCriteria criteria = SessionSearchMapper.toSearchCriteria(request);
        return sessionRepository.search(criteria);
    }

    //PATCH choroid/sessions/{id}
    public Session updateSession(UUID id, UpdateSessionRequest request)
    {
        UpdateFields updateFields = SessionUpdateMapper.toUpdateFields(request);
        return sessionRepository.update(id, updateFields);
    }

    //DELETE choroid/sessions/{id}
    public void deleteSession(UUID id)
    {
        sessionRepository.deleteById(id);
    }
}