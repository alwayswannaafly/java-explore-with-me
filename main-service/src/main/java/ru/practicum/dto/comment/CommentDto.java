package ru.practicum.dto.comment;

import lombok.Data;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private LocalDateTime createdOn;
    private UserShortDto author;
}