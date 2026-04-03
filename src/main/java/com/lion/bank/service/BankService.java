package com.lion.bank.service;

import java.math.BigDecimal;
import java.util.List;

import com.lion.bank.dto.ContactDTO;
import com.lion.bank.dto.TransactionDTO;
import com.lion.bank.dto.UserDTO;

public interface BankService {
    UserDTO getUserDetails(Long userId);
    void deposit(String accountNumber, BigDecimal amount);
    void withdraw(String accountNumber, BigDecimal amount);
    void transfer(Long currentUserId, String sourceAccount, String destinationAccount, BigDecimal amount);
    void moveToAccount(Long userId, String fromAccount, String toAccount, BigDecimal amount);
    List<TransactionDTO> getHistory(String accountNumber);
}
