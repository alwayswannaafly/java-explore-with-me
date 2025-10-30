package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.model.event.Location;

import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    @Min(1)
    private Long category;

    private Boolean paid;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;

    private String stateAction;
}
