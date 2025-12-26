package com.lion.bank.controller;

import com.lion.bank.dto.ContactDTO;
import com.lion.bank.dto.TransactionDTO;
import com.lion.bank.dto.TransactionRequestDTO;
import com.lion.bank.dto.UserDTO;
import com.lion.bank.entity.User;
import com.lion.bank.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RequestMapping("api/v1/bank")
@RestController
public class BankController {

    private final BankService bankService;

    @GetMapping("/user/me")
    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(bankService.getUserDetails(user.getId()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@Valid @RequestBody TransactionRequestDTO request){
        bankService.deposit(request.getSourceAccountNumber(), request.getAmount());
        return ResponseEntity.ok("Deposito realizado con exito");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@Valid @RequestBody TransactionRequestDTO request){
        bankService.withdraw(request.getSourceAccountNumber(), request.getAmount());
        return ResponseEntity.ok("Retiro realizado con exito");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @AuthenticationPrincipal User user, @RequestBody TransactionRequestDTO request){
        bankService.transfer(user.getId(),
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount());
        return ResponseEntity.ok("Transferencia realizada con exito");
    }

    @GetMapping("history/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getHistory(@PathVariable String accountNumber){
        return ResponseEntity.ok(bankService.getHistory(accountNumber));
    }

    /*@GetMapping("/user/me/contacts")
    public ResponseEntity<List<ContactDTO>> getContacts(User user){

    }*/
}
