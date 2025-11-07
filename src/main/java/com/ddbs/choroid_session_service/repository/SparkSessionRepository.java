package com.ddbs.choroid_session_service.repository;

import com.ddbs.choroid_session_service.dto.*;
import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.model.SessionRow;
import com.ddbs.choroid_session_service.model.SessionSpark;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.security.SaslOutputStream;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

@Slf4j
@Repository
public class SparkSessionRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SparkLogic sparkLogic;

    public SparkSessionRepository(SparkLogic sparkLogic) {
        this.sparkLogic = sparkLogic;
    }

    //GET choroid/sessions/count
    public Long countAllSessions()
    {
        return sparkLogic.getCachedData().count();
    }

    //GET choroid/sessions
    public List<Session> findAllSessions()
    {
        List<SessionSpark> sparkSessions = sparkLogic.getCachedData().collectAsList();
        return sparkSessions.stream().map(
                 ss -> {
                     try {
                         return convertSessionSparkToSession(ss);
                     } catch (JsonProcessingException e) {
                         throw new RuntimeException(e);
                     }
                 }).collect(Collectors.toList());
    }

    //GET choroid/sessions/{id}
    public Session findById(UUID id) throws JsonProcessingException {
        Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
        Dataset<SessionSpark> filtered = sessionData.filter(col("id").equalTo(id.toString()));

        // Check if any results exist before calling head()
        if (filtered.count() == 0) {
            log.error("Session with ID {} not found", id);
            throw new RuntimeException("Session with ID " + id + " not found");
        }
        return convertSessionSparkToSession(filtered.head());
    }

    //GET choroid/sessions/tags
    public List<String> findUniqueTags()
    {
        Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
        return sessionData.flatMap(new TagListFlatMapper(), Encoders.STRING()).distinct().collectAsList();
    }

    //POST choroid/sessions
    public Session save(Session session) throws JsonProcessingException {
        //Check for duplicate session (same creator_id, title, start)
        long count = 0L;
        try {
            Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
            count = sessionData.filter((col("creatorId").equalTo(session.getCreatorId())).and(col("title").equalTo(session.getTitle())).and(col("start").equalTo(session.getStart()))).count();
        } catch (Exception e) {
            log.error("Unexpected error checking for duplicate session", e);
            throw new RuntimeException("Unexpected error occurred while checking for duplicate session", e);
        }
        if (count > 0) {
            log.error("Duplicate session detected for creator_id: {}, title: {}, start: {}", session.getCreatorId(), session.getTitle(), session.getStart());
            throw new RuntimeException("A session with the same title and start date time already exists");
        }

        Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
        Dataset<SessionSpark> newRow = sparkLogic.createSessionSparkDatasetFromSession(session);
        sessionData = sessionData.unionByName(newRow, true);
        sparkLogic.saveToDatabase(sessionData);
        sparkLogic.refreshData();
        System.out.println(session.getId());
        long countTemp = countAllSessions();
        System.out.println(countTemp);
        return findById(session.getId());
    }

    //POST choroid/sessions/search
    public PageResponse<Session> search(SearchSessionRequest request) {
        try {
            Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
            if (request.getCreatorId() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value -> value.getCreatorId().equals(request.getCreatorId().trim()));

            if (request.getTitleContains() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value -> value.getTitle().contains(request.getTitleContains().trim()));

            if (request.getStartAfter() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value -> value.getStart().isAfter(request.getStartAfter()) || value.getStart().isEqual(request.getStartAfter()));

            if (request.getStartBefore() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value -> value.getStart().isBefore(request.getStartBefore()) || value.getStart().isEqual(request.getStartBefore()));

            if (request.getMinDuration() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value ->
                        value.getDuration() >= request.getMinDuration());

            if (request.getMaxDuration() != null)
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value ->
                        value.getDuration() <= request.getMaxDuration());

            if (request.getTagsInclude() != null) {
                sessionData = sessionData.filter((FilterFunction<SessionSpark>) value -> {
                    for (String item : request.getTagsInclude()) {
                        if (value.getTags().contains(item)) {
                            return true;
                        }
                    }
                    return false;
                });
            }

            // Sorting
            String orderBy = (request.getSortBy() != null) ? request.getSortBy() : "start";
            String sortDirection = (request.getSortOrder() != null && request.getSortOrder().equalsIgnoreCase("desc")) ? "desc" : "asc";
            if (sortDirection.equals("desc"))
                sessionData = sessionData.orderBy(desc(orderBy));
            else
                sessionData = sessionData.orderBy(asc(orderBy));

            //Pagination
            int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
            int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 20;
            int offset = page * size;

            //Get total count for pagination
            long totalItems = sessionData.count();
            int totalPages = (int) Math.ceil((double) totalItems / size);

            List<SessionSpark> collected = sessionData.collectAsList();

            List<SessionSpark> paged = collected.stream()
                    .skip(offset)
                    .limit(size)
                    .toList();

            return new PageResponse<Session>(
                    paged.stream().map(ss -> {
                        try {
                            return convertSessionSparkToSession(ss);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList()),
                    page,
                    size,
                    totalItems,
                    totalPages
            );
        } catch (Exception e) {
            log.error("Unexpected error searching sessions", e);
            throw new RuntimeException("Unexpected error occurred while searching sessions", e);
        }
    }

    //PATCH choroid/sessions/{id}
    public Session update(UUID id, UpdateSessionRequest request) throws JsonProcessingException {

        Dataset<SessionSpark> updatedData = sparkLogic.getCachedData();

        if (request.getTitle() != null) {
            updatedData = updatedData.withColumn("title",
                    when(col("id").equalTo(id.toString()), lit(request.getTitle()))
                            .otherwise(col("title"))).as(Encoders.bean(SessionSpark.class));
        }

        if (request.getStart() != null) {
            updatedData = updatedData.withColumn("start",
                    when(col("id").equalTo(id.toString()), lit(request.getStart()))
                            .otherwise(col("start"))).as(Encoders.bean(SessionSpark.class));
        }

        if (request.getDuration() != null) {
            updatedData = updatedData.withColumn("duration",
                    when(col("id").equalTo(id.toString()), lit(request.getDuration()))
                            .otherwise(col("duration"))).as(Encoders.bean(SessionSpark.class));
        }

        if (request.getMeetingLink() != null) {
            updatedData = updatedData.withColumn("meetingLink",
                    when(col("id").equalTo(id.toString()), lit(request.getMeetingLink()))
                            .otherwise(col("meetingLink"))).as(Encoders.bean(SessionSpark.class));
        }

        if (request.getResourcesLink() != null) {
            updatedData = updatedData.withColumn("resourcesLink",
                    when(col("id").equalTo(id.toString()), lit(request.getResourcesLink()))
                            .otherwise(col("resourcesLink"))).as(Encoders.bean(SessionSpark.class));
        }

        // Note: Tags (List) is trickier with Column API, use map for that
        if (request.getTags() != null) {
            final List<String> newTags = request.getTags();
            updatedData = updatedData.map(
                    (MapFunction<SessionSpark, SessionSpark>) session -> {
                        if (session.getId().equals(id.toString())) {
                            SessionSpark updated = new SessionSpark();
                            // Copy all fields
                            updated.setId(session.getId());
                            updated.setCreatorId(session.getCreatorId());
                            updated.setTitle(session.getTitle());
                            updated.setStart(session.getStart());
                            updated.setDuration(session.getDuration());
                            updated.setTags(new ArrayList<>(newTags));  // Update tags
                            updated.setMeetingLink(session.getMeetingLink());
                            updated.setResourcesLink(session.getResourcesLink());
                            return updated;
                        }
                        return session;
                    },
                    Encoders.bean(SessionSpark.class)
            );
        }

        sparkLogic.saveToDatabase(updatedData);
        sparkLogic.refreshData();
        return findById(id);
    }

    //DELETE choroid/sessions/{id}
    public void deleteById(UUID id) throws JsonProcessingException {
        Session session = findById(id);
        LocalDateTime end = session.getStart().plusMinutes(session.getDuration());
        if (end.isBefore(LocalDateTime.now())) {
            log.error("Attempt to delete past session with ID {}", id);
            throw new RuntimeException("Cannot delete a session that has already ended");
        }
        Dataset<SessionSpark> sessionData = sparkLogic.getCachedData();
        sessionData = sessionData.filter(col("id").notEqual(id.toString()));

        sparkLogic.saveToDatabase(sessionData);
        sparkLogic.refreshData();
    }

    private static Session convertSessionSparkToSession(SessionSpark sessionSpark) throws JsonProcessingException {

        return new Session(UUID.fromString(sessionSpark.getId()), sessionSpark.getCreatorId(), sessionSpark.getTitle(), sessionSpark.getStart(), sessionSpark.getDuration(), sessionSpark.getTags(), sessionSpark.getMeetingLink(), sessionSpark.getResourcesLink());
    }
}

class TagListFlatMapper implements FlatMapFunction<SessionSpark, String>, Serializable {

    @Override
    public Iterator<String> call(SessionSpark sessionSpark) throws Exception {
        return sessionSpark.getTags().iterator();
    }
}

