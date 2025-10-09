package com.ddbs.choroid_session_service.controller;

import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public List<Session> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @GetMapping("/{id}")
    public Optional<Session> getSessionById(@PathVariable Long id) {
        return sessionService.getSessionById(id);
    }

    @PostMapping
    public Session createSession(@RequestBody Session session) {
        return sessionService.createSession((session));
    }

    @PutMapping("/{id}")
    public Optional<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        return sessionService.updateSession(id, session);
    }
}
