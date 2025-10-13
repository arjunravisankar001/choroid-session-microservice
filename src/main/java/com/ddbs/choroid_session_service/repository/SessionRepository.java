package com.ddbs.choroid_session_service.repository;

import com.ddbs.choroid_session_service.model.Session;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Session> sessionRowMapper = (rs, rowNum) -> {
        Session session = new Session();
        session.setId(rs.getLong("id"));
        session.setCreatorId(rs.getString("creator_id"));
        session.setTitle(rs.getString("title"));
        session.setStart(rs.getTimestamp("start").toLocalDateTime());
        session.setDuration(rs.getInt("duration"));
        session.setTags(rs.getString("tags"));
        session.setMeetingLink(rs.getString("meeting_link"));
        session.setResourcesLink(rs.getString("resources_link"));
        return session;
    };

    public List<Session> findAll() {
        return jdbcTemplate.query("SELECT * FROM session", sessionRowMapper);
    }

    public Optional<Session> findById(Long id) {
        List<Session> results = jdbcTemplate.query("SELECT * FROM session WHERE id = ?", sessionRowMapper, id);
        return results.stream().findFirst();
    }

    public Session save(Session session) {
        jdbcTemplate.update(
                "INSERT INTO session (creator_id, title, start, duration, tags, meeting_link, resources_link) VALUES (?, ?, ?, ?, ?, ?, ?)",
                session.getCreatorId(),
                session.getTitle(),
                session.getStart(),
                session.getDuration(),
                session.getTags(),
                session.getMeetingLink(),
                session.getResourcesLink()
        );
        return session;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM session WHERE id = ?", id);
    }
}

