package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        testTransaction = new Transaction();
        testTransaction.setAccountNumber("123456");
        testTransaction.setAmount(100.0);

        // 模拟 AccountMapper 的行为
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(200.0);
        when(accountMapper.findByAccountNumber(testTransaction.getAccountNumber())).thenReturn(account);
    }

    @Test
    public void testCreateTransaction() throws InterruptedException {
        // 模拟 RedissonClient 的行为
        when(redissonClient.getLock(anyString())).thenReturn(lock);

        // 模拟 RLock 的行为
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        transactionService.createTransaction(testTransaction);

        verify(transactionMapper).insertTransaction(testTransaction);
        verify(accountMapper).updateAccount(any(Account.class));
        verify(lock).unlock();
    }

    @Test
    public void testCreateTransaction_InsufficientBalance() throws InterruptedException {
        // 模拟 AccountMapper 的行为
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(50.0);
        when(accountMapper.findByAccountNumber(testTransaction.getAccountNumber())).thenReturn(account);

        // 模拟 RedissonClient 的行为
        when(redissonClient.getLock(anyString())).thenReturn(lock);

        // 模拟 RLock 的行为
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(testTransaction));

        verify(transactionMapper, never()).insertTransaction(testTransaction);
        verify(accountMapper, never()).updateAccount(any(Account.class));
        verify(lock).unlock();
    }

    @Test
    public void testCreateTransaction_AccountNotFound() throws InterruptedException {
        // 模拟 AccountMapper 的行为
        when(accountMapper.findByAccountNumber(testTransaction.getAccountNumber())).thenReturn(null);

        // 模拟 RedissonClient 的行为
        when(redissonClient.getLock(anyString())).thenReturn(lock);

        // 模拟 RLock 的行为
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(testTransaction));

        verify(transactionMapper, never()).insertTransaction(testTransaction);
        verify(accountMapper, never()).updateAccount(any(Account.class));
        verify(lock).unlock();
    }
}
