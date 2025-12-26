package com.lion.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TransactionDTO {
    private Long id;
    private String type;
    private BigDecimal amount;
    private BigDecimal commission;
    private LocalDateTime timestamp;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
}
