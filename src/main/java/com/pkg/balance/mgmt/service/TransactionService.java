package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    public void createTransaction(Transaction transaction) {
        transactionMapper.insertTransaction(transaction);
    }
}
