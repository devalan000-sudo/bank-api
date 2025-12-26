package com.lion.bank.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ContactDTO {
    private Long id;
    private String name;
    private String email;
    private String accountNumber;
}
