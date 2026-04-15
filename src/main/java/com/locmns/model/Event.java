package com.locmns.model;

import com.locmns.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

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

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    protected String description;

    @Column(updatable = false, nullable = false)
    private LocalDateTime readingDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private EventType type;

    @ManyToOne
    protected Loan loan;

}
