package com.lion.bank.data;

import com.lion.bank.entity.Account;
import com.lion.bank.entity.User;
import com.lion.bank.enums.AccountType;
import com.lion.bank.repository.AccountRepository;
import com.lion.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class Data implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count()==0){
            System.out.println("===Creando Usuarios===");

            createUser("Alan", "alan@correo.com");
            createUser("Jesus", "jesus@correo.com");
            createUser("Ana", "ana@correo.com");

            System.out.println("===Usuarios creados exitosamente===");
        }
    }

    private void createUser(String name, String email){
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode("123"))
                .isVerified(true)
                .build();
        user = userRepository.save(user);

        Long userId = user.getId();

        Account checking = Account.builder()
                .user(user)
                .accountNumber(String.format("%03d", userId))
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("5000"))
                .build();
        accountRepository.save(checking);

        Account savings = Account.builder()
                .user(user)
                .accountNumber(String.format("%03d_1", userId))
                .type(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .build();
        accountRepository.save(savings);

        Account investment = Account.builder()
                .user(user)
                .accountNumber(String.format("%03d_2", userId))
                .type(AccountType.INVESTMENT)
                .balance(BigDecimal.ZERO)
                .build();
        accountRepository.save(investment);
    }
}
