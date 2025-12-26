package com.lion.bank.exceptions;

public class BankTransactionException extends RuntimeException {
    public BankTransactionException(String message) {
        super(message);
    }
}
