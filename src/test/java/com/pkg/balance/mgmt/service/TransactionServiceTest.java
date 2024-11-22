package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        testTransaction = new Transaction();
        testTransaction.setAccountNumber("123456");
        testTransaction.setAmount(100.0);
    }

    @Test
    public void testCreateTransaction() {
        when(transactionMapper.insertTransaction(testTransaction)).thenReturn(1);

        transactionService.createTransaction(testTransaction);

        verify(transactionMapper).insertTransaction(testTransaction);
    }
}
