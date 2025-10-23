package com.ddbs.choroid_session_service.repository;

import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.dto.PageResponse;
import com.ddbs.choroid_session_service.dto.UpdateFields;
import com.ddbs.choroid_session_service.dto.SearchCriteria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    //RowMapper to map ResultSet to Session object
    private final RowMapper<Session> sessionRowMapper = (rs, rowNum) -> {
        UUID id  = UUID.fromString(rs.getString("id"));
        String creatorId = rs.getString("creator_id");
        String title = rs.getString("title");
        LocalDateTime start = rs.getTimestamp("start").toLocalDateTime();
        Integer duration = rs.getInt("duration");
        String tagsJson = rs.getString("tags");
        List<String> tags = null;
        try {
            tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        }
        catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON string of tags", e);
        }
        String meetingLink = rs.getString("meeting_link");
        String resourcesLink = rs.getString("resources_link");
        return new Session(id, creatorId, title, start, duration, tags, meetingLink, resourcesLink);
    };

    private final RowMapper<Long> countRowMapper = (rs, rowNum) -> rs.getLong(1);

    //GET choroid/sessions/{id}
    public Session findById(UUID id)
    {
        String sql = "SELECT * FROM sessions WHERE id = ?";
        try {
            List<Session> results = jdbcTemplate.query(sql, sessionRowMapper, id);
            if (results.isEmpty()) {
                log.error("Session with ID {} not found", id);
                throw new RuntimeException("Session with ID " + id + " not found");
            }
            else {
                return results.getFirst();
            }
        }
        catch (DataAccessException e) {
            log.error("Database error occurred while fetching session by ID {}", id, e);
            throw new RuntimeException("Database error occurred while fetching session", e);
        }
        catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                throw (RuntimeException) e;
            }
            log.error("Unexpected error fetching session by ID {}", id, e);
            throw new RuntimeException("Unexpected error occurred while fetching session", e);
        }
    }

    //GET choroid/sessions/tags
    public Set<String> findUniqueTags()
    {
        String sql = "SELECT DISTINCT jsonb_array_elements_text(tags) AS tag FROM sessions";
        try {
            return Set.copyOf(jdbcTemplate.queryForList(sql, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Set.of();
        }
        catch (DataAccessException e) {
            log.error("Database error occurred while fetching unique tags", e);
            throw new RuntimeException("Database error occurred while fetching unique tags", e);
        }
        catch (Exception e) {
            log.error("Unexpected error fetching unique tags", e);
            throw new RuntimeException("Unexpected error occurred while fetching unique tags", e);
        }
    }

    //POST choroid/sessions
    public Session save(Session session) {
        //Check for duplicate session (same creator_id, title, start)
        String checkSql = "SELECT COUNT(*) FROM SESSIONS WHERE creator_id = ? AND title = ? AND start = ?";
        Long count = 0L;
        try {
            count = jdbcTemplate.queryForObject(checkSql, countRowMapper,
                    session.getCreatorId(),
                    session.getTitle(),
                    Timestamp.valueOf(session.getStart())
            );
        } catch (DataAccessException e) {
            log.error("Database error occurred while checking for duplicate session", e);
            throw new RuntimeException("Database error occurred while checking for duplicate session", e);
        } catch (Exception e) {
            log.error("Unexpected error checking for duplicate session", e);
            throw new RuntimeException("Unexpected error occurred while checking for duplicate session", e);
        }
        if (count != null && count > 0) {
            log.error("Duplicate session detected for creator_id: {}, title: {}, start: {}", session.getCreatorId(), session.getTitle(), session.getStart());
            throw new RuntimeException("A session with the same title and start date time already exists");
        }

        String sql = "INSERT INTO sessions (id, creator_id, title, start, duration, tags, meeting_link, resources_link) " +
                     "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?)";
        try {
            String tagsJson = objectMapper.writeValueAsString(session.getTags());
            jdbcTemplate.update(sql,
                    session.getId(),
                    session.getCreatorId(),
                    session.getTitle(),
                    Timestamp.valueOf(session.getStart()),
                    session.getDuration(),
                    tagsJson,
                    session.getMeetingLink(),
                    session.getResourcesLink()
            );
            return session;
        } catch (DataAccessException e) {
            log.error("Database error occurred while saving new session", e);
            throw new RuntimeException("Database error occurred while saving new session", e);
        } catch (JsonProcessingException e) {
            log.error("Error converting tags to JSON", e);
            throw new RuntimeException("Error processing tags for storage", e);
        }
        catch (Exception e) {
            log.error("Unexpected error saving new session", e);
            throw new RuntimeException("Unexpected error occurred while saving new session", e);
        }
    }

    //POST choroid/sessions/search
    public PageResponse<Session> search(SearchCriteria searchCriteria)
    {
        try {
            List<Session> sessions = jdbcTemplate.query(
                    searchCriteria.getSql(),
                    sessionRowMapper,
                    searchCriteria.getFinalParams()
            );
            //Get total count for pagination
            long totalItems = jdbcTemplate.query(searchCriteria.getCountSql(), countRowMapper, searchCriteria.getFilterParams()).getFirst();
            int totalPages = (int) Math.ceil((double) totalItems / searchCriteria.getLimit());
            return new PageResponse<Session>(
                    sessions,
                    searchCriteria.getOffset()/searchCriteria.getLimit(),
                    searchCriteria.getLimit(),
                    totalItems,
                    totalPages
            );
        }
        catch (EmptyResultDataAccessException e) {
            return new PageResponse<Session>(List.of(), 0, searchCriteria.getLimit(), 0, 0);
        }
        catch (DataAccessException e) {
            log.error("Database error occurred while searching sessions", e);
            throw new RuntimeException("Database error occurred while searching sessions", e);
        }
        catch (Exception e) {
            log.error("Unexpected error searching sessions", e);
            throw new RuntimeException("Unexpected error occurred while searching sessions", e);
        }
    }

    //PATCH choroid/sessions/{id}
    public Session update(UUID id, UpdateFields updateFields) {
        try {
            Session session = findById(id);
            String sql = updateFields.getSql();
            Object[] params = updateFields.getUpdateParams(id);
            if (sql.contains("start") && session.getStart().isBefore(LocalDateTime.now())) {
                log.error("Attempt to update start time of past session with ID {}", id);
                throw new RuntimeException("Cannot update start time of a session after its start time");
            }
            int rowsAffected = jdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                log.error("No rows affected while updating session with ID {}", id);
                throw new RuntimeException("Update failed, no rows affected");
            }
            session = findById(id);
            return session;
        } catch (DataAccessException e) {
            log.error("Database error occurred while updating session with ID {}", id, e);
            throw new RuntimeException("Database error occurred while updating session", e);
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                throw (RuntimeException) e;
            }
            log.error("Unexpected error updating session with ID {}", id, e);
            throw new RuntimeException("Unexpected error occurred while updating session", e);
        }
    }

    //DELETE choroid/sessions/{id}
    public void deleteById(UUID id) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        Session session = findById(id);
        LocalDateTime end = session.getStart().plusMinutes(session.getDuration());
        if (end.isBefore(LocalDateTime.now())) {
            log.error("Attempt to delete past session with ID {}", id);
            throw new RuntimeException("Cannot delete a session that has already ended");
        }
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected == 0) {
                log.error("No rows affected while deleting session with ID {}", id);
                throw new RuntimeException("Delete failed, no rows affected");
            }
        } catch (DataAccessException e) {
            log.error("Database error occurred while deleting session with ID {}", id, e);
            throw new RuntimeException("Database error occurred while deleting session", e);
        } catch (Exception e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("no rows affected")) {
                throw (RuntimeException) e;
            }
            log.error("Unexpected error deleting session with ID {}", id, e);
            throw new RuntimeException("Unexpected error occurred while deleting session", e);
        }
    }
}

