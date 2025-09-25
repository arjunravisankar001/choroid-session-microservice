package com.ddbs.choroid_session_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //unique session ID
    private String creatorId; //username of the session creator
    private String title; //session title
    private LocalDateTime start; //start date and time of the session
    private Integer duration; //session duration in minutes
    private String tags; //session-relevant tags
    private String meetingLink; //link to the session
    private String resourcesLink; //link to session resources

    //Constructors, getters and setters

    public Session() {

    }

    public Session(String creatorId, String title, LocalDateTime start, Integer duration, String tags, String meetingLink, String resourcesLink) {
        this.creatorId = creatorId;
        this.title = title;
        this.start = start;
        this.duration = duration;
        this.tags = tags;
        this.meetingLink = meetingLink;
        this.resourcesLink = resourcesLink;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getResourcesLink() {
        return resourcesLink;
    }

    public void setResourcesLink(String resourcesLink) {
        this.resourcesLink = resourcesLink;
    }
}
