package ru.practicum.dto.compilation;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private String title;
    private Boolean pinned;
    private List<Long> events;
}