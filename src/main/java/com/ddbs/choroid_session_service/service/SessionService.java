package com.ddbs.choroid_session_service.service;

import com.ddbs.choroid_session_service.dto.*;
import com.ddbs.choroid_session_service.mapper.SessionCreateMapper;
import com.ddbs.choroid_session_service.mapper.SessionSearchMapper;
import com.ddbs.choroid_session_service.mapper.SessionUpdateMapper;
import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.repository.SparkSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    public SparkSessionRepository sparkSessionRepository;

    public SessionService(SparkSessionRepository sparkSessionRepository) {
        this.sparkSessionRepository = sparkSessionRepository;
    }

    //GET choroid/sessions/count
    public Long getSessionCount() {
        return sparkSessionRepository.countAllSessions();
    }

    //GET choroid/sessions
    public List<Session> getAllSessions() {
        return sparkSessionRepository.findAllSessions();
    }

    //GET choroid/sessions/{id}
    public Session getSessionById(UUID id) throws JsonProcessingException {
        return sparkSessionRepository.findById(id);
    }

    //GET choroid/sessions/tags
    public List<String> getTags()
    {
        return sparkSessionRepository.findUniqueTags();
    }

    //POST choroid/sessions
    public Session createSession(CreateSessionRequest request) throws JsonProcessingException {
        Session session = SessionCreateMapper.fromCreateRequest(request);
        return sparkSessionRepository.save(session);
    }

    //POST choroid/sessions/search
    public PageResponse<Session> searchSessions(SearchSessionRequest request)
    {
//        SearchCriteria criteria = SessionSearchMapper.toSearchCriteria(request);
        return sparkSessionRepository.search(request);
    }

    //PATCH choroid/sessions/{id}
    public Session updateSession(UUID id, UpdateSessionRequest request) throws JsonProcessingException {
//        UpdateFields updateFields = SessionUpdateMapper.toUpdateFields(request);
        return sparkSessionRepository.update(id, request);
    }

    //DELETE choroid/sessions/{id}
    public void deleteSession(UUID id) throws JsonProcessingException {
        sparkSessionRepository.deleteById(id);
    }
}