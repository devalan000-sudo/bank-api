package com.lion.bank.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorDTO {
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
