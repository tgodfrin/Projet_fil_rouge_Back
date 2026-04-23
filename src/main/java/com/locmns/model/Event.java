package com.locmns.model;

import com.locmns.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    protected String description;

    @Column(nullable = true)
    private LocalDateTime readingDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private EventType type;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected Loan loan;

}
