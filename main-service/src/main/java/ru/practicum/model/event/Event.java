package ru.practicum.model.event;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.Category;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Embedded
    private Location location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column
    @Builder.Default
    private Integer participantLimit = 0;

    @Column
    @Builder.Default
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventState state = EventState.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();

    private LocalDateTime publishedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
