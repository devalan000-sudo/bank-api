package com.lion.bank.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lion.bank.entity.Account;
import com.lion.bank.entity.User;
import com.lion.bank.enums.AccountType;
import com.lion.bank.repository.AccountRepository;
import com.lion.bank.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public User register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es requerido");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }

        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        String verificationToken = generateVerificationToken();

        User user = User.builder()
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .isVerified(false)
                .verificationToken(verificationToken)
                .build();

        user = userRepo.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return user;
    }

    public User verifyEmail(String token) {
        User user = userRepo.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de verificación inválido"));

        if (user.isVerified()) {
            throw new RuntimeException("La cuenta ya ha sido verificada");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user = userRepo.save(user);

        createAccountsForUser(user);

        return user;
    }

    private String generateVerificationToken() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void createAccountsForUser(User user) {
        Long userId = user.getId();
        
        Account checking = Account.builder()
                .accountNumber(String.format("%03d", userId))
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        accountRepo.save(checking);

        Account savings = Account.builder()
                .accountNumber(String.format("%03d_1", userId))
                .type(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        accountRepo.save(savings);

        Account investment = Account.builder()
                .accountNumber(String.format("%03d_2", userId))
                .type(AccountType.INVESTMENT)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        accountRepo.save(investment);
    }
}
