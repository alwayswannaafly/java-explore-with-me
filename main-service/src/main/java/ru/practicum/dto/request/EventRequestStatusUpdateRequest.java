package ru.practicum.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private String status; // "CONFIRMED" | "REJECTED"
}