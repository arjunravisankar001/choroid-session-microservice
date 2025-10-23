package com.ddbs.choroid_session_service.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Session {

    private UUID id; //unique session ID
    private String creatorId; //username of the session creator
    private String title; //session title
    private LocalDateTime start; //start date and time of the session
    private Integer duration; //session duration in minutes
    private List<String> tags; //session-applicable tags
    private String meetingLink; //link to the session
    private String resourcesLink; //link to session resources

}
