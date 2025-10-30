package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();
}