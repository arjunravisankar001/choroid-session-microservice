package com.ddbs.choroid_session_service.dto;

import com.ddbs.choroid_session_service.validation.NotBlankIfPresent;
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
public class UpdateSessionRequest {

    @NotBlankIfPresent(message = "Session title must be non-empty if present")
    private String title;

    @Future(message = "You cannot reschedule a session to the past, unless you have a Time Turner")
    private LocalDateTime start;

    @Min(value = 1, message = "Session duration must be positive if present")
    private Integer duration;

    @Size(min = 1, message = "At least one tag is required if tags are provided")
    private List<@NotBlank String> tags;

    @NotBlankIfPresent(message = "Meeting link must be non-empty if present")
    @URL(protocol = "https", message = "Meeting link must be a valid HTTPS URL if present")
    private String meetingLink;

    @NotBlankIfPresent(message = "Resources link must be non-empty if present")
    @URL(protocol = "https", message = "Resources link must be a valid HTTPS URL if present")
    private String resourcesLink;

}
