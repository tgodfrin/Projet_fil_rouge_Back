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
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    private LocalDateTime beginDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = true)
    private LocalDateTime realEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLoanType statusType;

    @Column(nullable = false)
    private LocalDateTime statusDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected AppUser requester;

    @ManyToOne
    @JoinColumn(nullable = true)
    protected AppUser validator;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected Equipment equipment;


}