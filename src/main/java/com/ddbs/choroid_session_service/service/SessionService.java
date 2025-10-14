package com.ddbs.choroid_session_service.service;

import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    public Optional<Session> updateSession(Long id, Session session) {
        return sessionRepository.findById(id).map(existingSession -> {
            existingSession.setCreatorId(session.getCreatorId());
            existingSession.setTitle(session.getTitle());
            existingSession.setStart(session.getStart());
            existingSession.setDuration(session.getDuration());
            existingSession.setTags(session.getTags());
            existingSession.setMeetingLink(session.getMeetingLink());
            existingSession.setResourcesLink(session.getResourcesLink());
            return sessionRepository.update(existingSession);
        });
    }
}