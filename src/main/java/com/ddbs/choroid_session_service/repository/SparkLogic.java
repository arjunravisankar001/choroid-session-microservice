package com.ddbs.choroid_session_service.repository;

import com.ddbs.choroid_session_service.dto.PageResponse;
import com.ddbs.choroid_session_service.dto.SearchCriteria;
import com.ddbs.choroid_session_service.dto.UpdateFields;
import com.ddbs.choroid_session_service.model.Session;
import com.ddbs.choroid_session_service.model.SessionRow;
import com.ddbs.choroid_session_service.model.SessionSpark;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.apache.spark.sql.functions.lit;

@Slf4j
@Service
@EnableAsync
public class SparkLogic {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SparkSession sparkSession;
    private Dataset<SessionSpark> sessionDataset;
    private volatile boolean sparkInitialized = false;
    private volatile boolean dataReady = false;

    @Value("${spring.datasource.url}")
    String jdbcUrl;

    @Value("${spring.datasource.username}")
    String dbUsername;

    @Value("${spring.datasource.password}")
    String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    String datasourceDriver;

    @Value("${spark-port}")
    String sparkPort;

    @EventListener(ApplicationReadyEvent.class)
    public void initSpark() {
        System.out.println("Initializing Spark Session...");
        System.out.println("Java Version: " + System.getProperty("java.version"));

        // Minimal Windows compatibility for the driver (not needed by Docker containers)
        System.setProperty("HADOOP_USER_NAME", "spark");
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("hadoop.home.dir", System.getProperty("java.io.tmpdir"));
        }

        sparkSession = SparkSession.builder()
                .appName("Session Microservice")
                .master("local[*]")
                .config("spark.driver.memory", "2g")
//                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.jars", "./libs/h2.jar")
                .config("spark.ui.enabled", "true")
                .config("spark.ui.port", sparkPort)
                .config("spark.driver.extraJavaOptions",
                        "--add-opens=java.base/java.lang=ALL-UNNAMED " +
                                "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED " +
                                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED " +
                                "--add-opens=java.base/java.io=ALL-UNNAMED " +
                                "--add-opens=java.base/java.nio=ALL-UNNAMED " +
                                "--add-opens=java.base/java.util=ALL-UNNAMED " +
                                "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
                                "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED " +
                                "--add-opens=java.base/sun.security.util=ALL-UNNAMED")
                .getOrCreate();

        System.out.println("Spark Session connected to cluster");
        sparkInitialized = true;

        // Load data asynchronously - don't block application startup
        loadDataAsync();

//        refreshData();
    }

    @Async
    public CompletableFuture<Void> loadDataAsync() {
        try {
            System.out.println("Loading data from database asynchronously...");
            refreshData();
            dataReady = true;
            System.out.println("Data loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public synchronized void refreshData() {
        if (sessionDataset != null)
            sessionDataset.unpersist();

        sessionDataset = loadFromDatabase().persist(StorageLevel.MEMORY_AND_DISK());

        sessionDataset.count(); //just as a trigger
//        return sessionDataset;
    }

    public Dataset<SessionSpark> getCachedData() {
        // Wait for Spark to be initialized
        waitForSparkInitialization();

        if (sessionDataset == null)
            refreshData();
        return sessionDataset;
    }

    public Dataset<SessionSpark> createSessionSparkDatasetFromSession(Session session) {
        waitForSparkInitialization();
        List<SessionSpark> tempSession = Collections.singletonList(new SessionSpark(session.getId().toString(), session.getCreatorId(), session.getTitle(), session.getStart(), session.getDuration(), session.getTags(), session.getMeetingLink(), session.getResourcesLink()));
        return sparkSession.createDataset(tempSession, Encoders.bean(SessionSpark.class));
    }

    private void waitForSparkInitialization() {
        int maxWaitSeconds = 60;
        int waitedSeconds = 0;
        while (!sparkInitialized && waitedSeconds < maxWaitSeconds) {
            try {
                System.out.println("⏳ Waiting for Spark initialization...");
                Thread.sleep(1000);
                waitedSeconds++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Spark initialization", e);
            }
        }

        if (!sparkInitialized) {
            throw new RuntimeException("Spark failed to initialize within " + maxWaitSeconds + " seconds");
        }
    }

    public void saveToDatabase(Dataset<SessionSpark> data) {

        Encoder<SessionRow> sessionRowEncoder = Encoders.bean(SessionRow.class);

        // Now map to SessionRow
        Dataset<SessionRow> dbData = data.map(
                (MapFunction<SessionSpark, SessionRow>) SparkLogic::convertSessionSparkToSessionRow,
                sessionRowEncoder
        );

        dbData.write().format("jdbc").option("url", jdbcUrl)
                .option("dbtable", "SESSIONS")
                .option("user", dbUsername)
                .option("password", dbPassword)
                .option("driver", datasourceDriver).mode("Overwrite").save();
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (sessionDataset != null)
            sessionDataset.unpersist();
        if (sparkSession != null)
            sparkSession.close();
    }

    private Dataset<SessionSpark> loadFromDatabase() {

        Encoder<SessionSpark> sessionSparkEncoder = Encoders.bean(SessionSpark.class);
        Encoder<SessionRow> sessionRowEncoder = Encoders.bean(SessionRow.class);

        // Use the same TCP H2 database for both driver and executors
        // String executorJdbcUrl = jdbcUrl.replace("localhost", "h2-database");

        Dataset<Row> tempData = sparkSession.read().format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "SESSIONS")
                .option("user", dbUsername)
                .option("password", dbPassword)
                .option("driver", datasourceDriver)
                .load();

        Dataset<SessionRow> sessionRowDataset = tempData.as(sessionRowEncoder);

        // Now map to SessionSpark
        return sessionRowDataset.map(
                (MapFunction<SessionRow, SessionSpark>) SparkLogic::convertSessionRowToSessionSpark,
                sessionSparkEncoder
        );
    }

    private static SessionSpark convertSessionRowToSessionSpark (SessionRow sessionRow) throws SQLException {

        ObjectMapper objectMapper = new ObjectMapper();
        String id  = sessionRow.getId();
        String creatorId = sessionRow.getCreator_id();
        String title = sessionRow.getTitle();
        LocalDateTime start = sessionRow.getStart().toLocalDateTime();
        Integer duration = sessionRow.getDuration();
        String tagsJson = sessionRow.getTags();
        List<String> tags = null;
        try {
            tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        }
        catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON string of tags", e);
        }
        String meetingLink = sessionRow.getMeeting_link();
        String resourcesLink = sessionRow.getResources_link();
        return new SessionSpark(id, creatorId, title, start, duration, tags, meetingLink, resourcesLink);
    }

    private static SessionRow convertSessionSparkToSessionRow(SessionSpark sessionSpark) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String tagsJson = objectMapper.writeValueAsString(sessionSpark.getTags());
        return new SessionRow(sessionSpark.getId(), sessionSpark.getCreatorId(), sessionSpark.getTitle(), Timestamp.valueOf(sessionSpark.getStart()), sessionSpark.getDuration(), tagsJson, sessionSpark.getMeetingLink(), sessionSpark.getResourcesLink());

    }
}

