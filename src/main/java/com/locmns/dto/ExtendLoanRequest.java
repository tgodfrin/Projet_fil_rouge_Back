package com.locmns.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExtendLoanRequest {

    @NotNull
    private LocalDate newEndDate;
}
