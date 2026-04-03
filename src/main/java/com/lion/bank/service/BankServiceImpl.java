package com.lion.bank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.lion.bank.repository.TransactionRepository;
import com.lion.bank.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transRepo;

    @Override
    public UserDTO getUserDetails(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("No se pudo encontrar el usuario"));
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accounts(user.getAccounts().stream().map(account -> AccountDTO.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .type(account.getType().name())
                        .typeDisplay(getTypeDisplay(account.getType()))
                        .balance(account.getBalance())
                        .build()).collect(Collectors.toList()))
                .build();

    }

    private String getTypeDisplay(AccountType type) {
        return switch (type) {
            case CHECKING -> "Cuenta Corriente";
            case SAVINGS -> "Cuenta de Ahorros";
            case INVESTMENT -> "Inversión";
        };
    }


    @Override
    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        if (account.getType() != AccountType.CHECKING) {
            throw new BankTransactionException("Solo se puede depositar a cuentas corrientes");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        saveTransaction(account, amount, BigDecimal.ZERO, TransactionType.DEPOSIT, null, null);
    }

    @Override
    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        if (account.getType() != AccountType.CHECKING) {
            throw new BankTransactionException("Solo se puede retirar de cuentas corrientes");
        }

        if(account.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Fondos insuficientes!");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        saveTransaction(account, amount, BigDecimal.ZERO,  TransactionType.WITHDRAWAL, null, null);
    }

    @Override
    @Transactional
    public void transfer(Long currentUserId, String sourceAccount, String destinationAccount, BigDecimal amount){
        Account srcAccount = accountRepo.findByAccountNumber(sourceAccount)
        .orElseThrow(()-> new ResourceNotFoundException("La cuenta origen no fue encontrada"));

        if (srcAccount.getType() != AccountType.CHECKING) {
            throw new BankTransactionException("Solo se puede transferir desde cuentas corrientes");
        }

        if(!srcAccount.getUser().getId().equals(currentUserId)){
            throw new BankTransactionException("La cuenta origen no pertenece al usuario autenticado");
        }

        Account dest = accountRepo.findByAccountNumber(destinationAccount)
        .orElseThrow(()-> new ResourceNotFoundException("Cuenta Destino no fue encontrada"));

        if(sourceAccount.equals(destinationAccount)){
            throw new BankTransactionException("La cuenta origen y destino no pueden ser las mismas");
        }

        boolean isOwnAccount = srcAccount.getUser().getId().equals(dest.getUser().getId());
        BigDecimal commission = isOwnAccount ? BigDecimal.ZERO : new BigDecimal("3.00");
        BigDecimal totalDeduction = amount.add(commission);

        if(srcAccount.getBalance().compareTo(totalDeduction) < 0){
            throw new InsufficientBalanceException("Fondos insuficientes para realizar la transaccion");
        }

        srcAccount.setBalance(srcAccount.getBalance().subtract(totalDeduction));
        dest.setBalance(dest.getBalance().add(amount));

        accountRepo.save(srcAccount);
        accountRepo.save(dest);

        saveTransaction(srcAccount, amount, commission, isOwnAccount ? TransactionType.TRANSFER_OWN : 
            TransactionType.TRANSFER_THIRD, sourceAccount, destinationAccount);
        
    }

    @Override
    @Transactional
    public void moveToAccount(Long userId, String fromAccount, String toAccount, BigDecimal amount) {
        Account srcAccount = accountRepo.findByAccountNumber(fromAccount)
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta origen no encontrada"));

        if (!srcAccount.getUser().getId().equals(userId)) {
            throw new BankTransactionException("La cuenta origen no pertenece al usuario");
        }

        Account destAccount = accountRepo.findByAccountNumber(toAccount)
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta destino no encontrada"));

        if (!destAccount.getUser().getId().equals(userId)) {
            throw new BankTransactionException("La cuenta destino no pertenece al usuario");
        }

        if (fromAccount.equals(toAccount)) {
            throw new BankTransactionException("La cuenta origen y destino no pueden ser las mismas");
        }

        if (srcAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Fondos insuficientes");
        }

        srcAccount.setBalance(srcAccount.getBalance().subtract(amount));
        destAccount.setBalance(destAccount.getBalance().add(amount));

        accountRepo.save(srcAccount);
        accountRepo.save(destAccount);

        saveTransaction(srcAccount, amount, BigDecimal.ZERO, TransactionType.TRANSFER_OWN, fromAccount, toAccount);
        saveTransaction(destAccount, amount, BigDecimal.ZERO, TransactionType.DEPOSIT, fromAccount, toAccount);
    }

    @Override
    public List<TransactionDTO> getHistory(String accountNumber) {
        Account accountHis = accountRepo.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        return transRepo.findByAccountIdOrderByTimestampDesc(accountHis.getId())
        .stream()
        .map(tran -> TransactionDTO.builder()
        .id(tran.getId())
        .type(tran.getType().name())
        .amount(tran.getAmount())
        .commission(tran.getCommission())
        .timestamp(tran.getTimestamp())
        .sourceAccountNumber(tran.getSourceAccountNumber())
        .destinationAccountNumber(tran.getDestinationAccountNumber())
        .build())
        .collect(Collectors.toList());

    }

    public void saveTransaction(Account account, BigDecimal amount, BigDecimal commission, TransactionType type, String src, String dst){
        Transaction trans = Transaction.builder()
        .account(account)
        .amount(amount)
        .commission(commission)
        .type(type)
        .timestamp(LocalDateTime.now())
        .sourceAccountNumber(src)
        .destinationAccountNumber(dst)
        .build();
        transRepo.save(trans);
    }

}
