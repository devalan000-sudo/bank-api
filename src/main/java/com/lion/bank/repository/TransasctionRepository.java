package com.lion.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lion.bank.entity.Transaction;

public interface TransasctionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId);
}
