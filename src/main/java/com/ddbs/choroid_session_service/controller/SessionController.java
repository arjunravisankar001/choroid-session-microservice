package com.ddbs.choroid_session_service.controller;

import com.ddbs.choroid_session_service.dto.CreateSessionRequest;
import com.ddbs.choroid_session_service.dto.PageResponse;
import com.ddbs.choroid_session_service.dto.SearchSessionRequest;
import com.ddbs.choroid_session_service.dto.UpdateSessionRequest;
import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/choroid/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/count")
    public Long getSessionCount() {
        return sessionService.getSessionCount();
    }

    @GetMapping
    public List<Session> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @GetMapping("/{id}")
    public Session getSessionById(@PathVariable UUID id) {
        return sessionService.getSessionById(id);
    }

    @GetMapping("/tags")
    public List<String> getTags() {
        return sessionService.getTags();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Session createSession(@RequestBody @Valid CreateSessionRequest request) {
        return sessionService.createSession(request);
    }

    @PostMapping("/search")
    public PageResponse<Session> searchSessions(@RequestBody @Valid SearchSessionRequest request)
    {
        return sessionService.searchSessions(request);
    }

    @PatchMapping("/{id}")
    public Session updateSession(@PathVariable UUID id, @RequestBody @Valid UpdateSessionRequest request)
    {
        return sessionService.updateSession(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable UUID id) {
        sessionService.deleteSession(id);
    }
}
