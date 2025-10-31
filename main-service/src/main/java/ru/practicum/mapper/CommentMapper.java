package ru.practicum.mapper;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;

public class CommentMapper {
    public static Comment toComment(NewCommentDto dto, User author, Event event) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(java.time.LocalDateTime.now());
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedOn(comment.getCreatedOn());
        dto.setAuthor(UserMapper.toUserShortDto(comment.getAuthor()));
        return dto;
    }
}
