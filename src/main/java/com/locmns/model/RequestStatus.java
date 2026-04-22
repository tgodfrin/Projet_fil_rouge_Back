package com.locmns.model;

import com.locmns.enums.StatusLoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false,updatable = false)
    private LocalDateTime statusDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private StatusLoanType statusType;

}
