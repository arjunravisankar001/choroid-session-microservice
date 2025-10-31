package com.ddbs.choroid_session_service.dto;

import com.ddbs.choroid_session_service.validation.NotBlankIfPresent;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchSessionRequest implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlankIfPresent(message = "Creator ID must be non-empty if present")
    private String creatorId;

    @NotBlankIfPresent(message = "Title substring must be non-empty if present")
    private String titleContains;

    private LocalDateTime startAfter;
    private LocalDateTime startBefore;

    @Min(value = 1, message = "Minimum duration must be positive if present")
    private Integer minDuration;
    @Min(value = 1, message = "Maximum duration must be positive if present")
    private Integer maxDuration;

    @Size(min = 1, message = "At least one tag is required if tags are present")
    private List<@NotBlank String> tagsInclude;

    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page; //Page number for pagination (0-indexed)

    @Min(value = 1, message = "Page size must be at least 1") //the user can't ask for zero or negative items
    @Max(value = 100, message = "Page size must not exceed 100") //prevents abuse (e.g., requesting 1,000,000 results at once)
    private Integer size; //Number of sessions per page

    @Pattern(regexp = "start|duration|title|creator_id", message = "sortBy must be one of: start, duration, title, creator_id") //Prevents SQL injection or runtime errors on invalid column names
    private String sortBy; //Field to sort by (e.g., "start", "duration")

    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE, message = "sortOrder must be either 'asc' or 'desc'") //Prevents SQL injection or runtime errors on invalid sort orders
    private String sortOrder; //Sort order: "asc" for ascending, "desc" for descending

}
