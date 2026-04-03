package com.lion.bank.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String type;
    private String typeDisplay;
    private BigDecimal balance;
}
