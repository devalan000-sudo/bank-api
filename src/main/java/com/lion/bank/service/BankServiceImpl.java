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
public class BankServiceImpl implements BankService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final TransasctionRepository transRepo;

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
                        .balance(account.getBalance())
                        .build()).collect(Collectors.toList()))
                .build();

    }


    @Override
    public void deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        saveTransaction(account, amount, BigDecimal.ZERO, TransactionType.DEPOSIT, null, null);
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

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

        if(!srcAccount.getUser().getId().equals(currentUserId)){
            throw new BankTransactionException("La cuenta origen no pertenece al usuario autenticado");
        }

        Account dest = accountRepo.findByAccountNumber(destinationAccount)
        .orElseThrow(()-> new ResourceNotFoundException("Cuenta Destino no fue encontrada"));

        if(sourceAccount.equals(destinationAccount)){
            throw new BankTransactionException("La cuenta origen y destino no pueden ser las mismas");
        }

        boolean isOwnAccount = srcAccount.getUser().getId().equals(dest.getUser().getId());
        BigDecimal commission = isOwnAccount ? BigDecimal.ZERO : new BigDecimal("1.50");
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
        .type(type)
        .timestamp(LocalDateTime.now())
        .sourceAccountNumber(src)
        .destinationAccountNumber(dst)
        .build();
        transRepo.save(trans);
    }

}
