package com.lion.bank.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lion.bank.dto.AccountDTO;
import com.lion.bank.dto.TransactionDTO;
import com.lion.bank.dto.UserDTO;
import com.lion.bank.entity.Account;
import com.lion.bank.entity.Transaction;
import com.lion.bank.entity.User;
import com.lion.bank.enums.AccountType;
import com.lion.bank.enums.TransactionType;
import com.lion.bank.exceptions.BankTransactionException;
import com.lion.bank.exceptions.InsufficientBalanceException;
import com.lion.bank.exceptions.ResourceNotFoundException;
import com.lion.bank.repository.AccountRepository;
import com.lion.bank.repository.TransasctionRepository;
import com.lion.bank.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;

    public User register(String name, String email, String password) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        user = userRepo.save(user);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        accountRepo.save(account);

        return user;
    }

    private String generateAccountNumber() {
        return "CLA" + System.currentTimeMillis();
    }
}
