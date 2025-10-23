package com.ddbs.choroid_session_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateSessionRequest {

    @NotBlank(message = "Session creator's user ID must be provided")
    private String creatorId;

    @NotBlank(message = "Session title must be provided")
    private String title;

    @NotNull(message = "Session start date and time must be provided")
    @Future(message = "You cannot schedule a session in the past, unless you have a Time Turner")
    private LocalDateTime start;

    @NotNull(message = "Session duration must be provided")
    @Min(value = 1, message = "Session duration must be positive")
    private Integer duration;

    @NotNull(message = "Session-applicable tags must be provided")
    @Size(min = 1, message = "At least one tag is required")
    private List<@NotBlank(message = "Tag must be non-empty") String> tags;

    @NotBlank(message = "Session meeting link must be provided")
    @URL(protocol = "https", message = "Meeting link must be a valid HTTPS URL")
    private String meetingLink;

    @NotBlank(message = "Session resources link must be provided")
    @URL(protocol = "https", message = "Resources link must be a valid HTTPS URL")
    private String resourcesLink;

}
