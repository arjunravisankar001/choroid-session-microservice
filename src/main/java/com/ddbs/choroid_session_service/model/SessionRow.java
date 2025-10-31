package com.ddbs.choroid_session_service.model;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SessionRow {

    private String id; //unique session ID
    private String creator_id; //username of the session creator
    private String title; //session title
    private Timestamp start; //start date and time of the session
    private Integer duration; //session duration in minutes
    private String tags; //session-applicable tags
    private String meeting_link; //link to the session
    private String resources_link; //link to session resources

}
