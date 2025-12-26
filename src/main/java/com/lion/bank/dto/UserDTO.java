package com.lion.bank.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private List<AccountDTO> accounts;
}
