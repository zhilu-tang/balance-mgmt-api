package com.pkg.balance.mgmt.service;

import com.github.javafaker.Faker;
import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionService transactionServiceMock;

    private Faker faker;

    @Mock
    private RLock rLock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        faker = new Faker();
    }

    @Test
    void testCreateTransactionSuccess() throws InterruptedException {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 100, 1000);

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber(destinationAccountNumber);
        destinationAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 模拟方法调用
        when(accountMapper.findByAccountNumber(sourceAccountNumber)).thenReturn(sourceAccount);
        when(accountMapper.findByAccountNumber(destinationAccountNumber)).thenReturn(destinationAccount);

        RLock sourceLock = mock(RLock.class);
        RLock destinationLock = mock(RLock.class);
        when(redissonClient.getLock(any(String.class))).thenReturn(sourceLock, destinationLock);
        when(sourceLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(destinationLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);

        // 调用方法
        transactionService.createTransaction(transaction);

        // 验证方法调用
        verify(accountMapper).updateAccount(sourceAccount);
        verify(accountMapper).updateAccount(destinationAccount);
        verify(transactionMapper).insertTransaction(transaction);
    }

    @Test
    void testCreateTransactionInsufficientBalance() throws InterruptedException {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 1000, 5000);

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(faker.number().randomDouble(2, 100, 500));

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber(destinationAccountNumber);
        destinationAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 模拟方法调用
        when(accountMapper.findByAccountNumber(sourceAccountNumber)).thenReturn(sourceAccount);
        when(accountMapper.findByAccountNumber(destinationAccountNumber)).thenReturn(destinationAccount);

        RLock sourceLock = mock(RLock.class);
        RLock destinationLock = mock(RLock.class);
        when(redissonClient.getLock(any(String.class))).thenReturn(sourceLock, destinationLock);
        when(sourceLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(destinationLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);

        // 调用方法并捕获异常
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transaction));

        // 验证方法调用
        verify(accountMapper, never()).updateAccount(sourceAccount);
        verify(accountMapper, never()).updateAccount(destinationAccount);
        verify(transactionMapper, never()).insertTransaction(transaction);
    }

    @Test
    void testCreateTransactionAccountNotFound() throws InterruptedException {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 100, 1000);

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 模拟方法调用
        when(accountMapper.findByAccountNumber(sourceAccountNumber)).thenReturn(null);

        RLock sourceLock = mock(RLock.class);
        RLock destinationLock = mock(RLock.class);
        when(redissonClient.getLock(any(String.class))).thenReturn(sourceLock, destinationLock);
        when(sourceLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(destinationLock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);

        // 调用方法并捕获异常
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transaction));

        // 验证方法调用
        verify(accountMapper, never()).updateAccount(any(Account.class));
        verify(transactionMapper, never()).insertTransaction(transaction);
    }

    @Test
    public void testCreateTransactionFailureAndRetry() throws Exception {
        // 模拟失败情况
        // 生成测试数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 100, 1000);

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(faker.number().randomDouble(2, 100, 500));

        // 模拟方法调用
        when(accountMapper.findByAccountNumber(anyString())).thenReturn(sourceAccount);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);

        // 模拟发送消息失败
        doThrow(new RuntimeException("Failed to send transaction to retry queue"))
                .when(transactionServiceMock)
                .sendToRetryQueue(any(Transaction.class));

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        try {
            transactionServiceMock.createTransaction(transaction);
        } catch (RuntimeException e) {
            // 验证发送重试队列的方法被调用
            verify(transactionServiceMock, times(1)).sendToRetryQueue(any(Transaction.class));
        }
    }
}
