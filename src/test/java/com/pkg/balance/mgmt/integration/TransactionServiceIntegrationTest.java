package com.pkg.balance.mgmt.integration;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.integration.config.TestConfig;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import com.github.javafaker.Faker;
import com.pkg.balance.mgmt.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private RedissonClient redissonClient;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        // 清空数据库
        accountMapper.deleteAllAccounts();
        transactionMapper.deleteAllTransactions();
    }

    @Test
    void testCreateTransactionSuccess() {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 100, 1000);

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));
        accountMapper.insertAccount(sourceAccount);

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber(destinationAccountNumber);
        destinationAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));
        accountMapper.insertAccount(destinationAccount);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 调用方法
        transactionService.createTransaction(transaction);

        // 验证结果
        Account updatedSourceAccount = accountMapper.findByAccountNumber(sourceAccountNumber);
        assertNotNull(updatedSourceAccount);
        assertEquals(sourceAccount.getBalance() - amount, updatedSourceAccount.getBalance());

        Account updatedDestinationAccount = accountMapper.findByAccountNumber(destinationAccountNumber);
        assertNotNull(updatedDestinationAccount);
        assertEquals(destinationAccount.getBalance() + amount, updatedDestinationAccount.getBalance());

        List<Transaction> transactions = transactionMapper.getAllTransactions();
        assertEquals(1, transactions.size());
        assertEquals(sourceAccountNumber, transactions.get(0).getAccountNumber());
        assertEquals(destinationAccountNumber, transactions.get(0).getDestinationAccountNumber());
        assertEquals(amount, transactions.get(0).getAmount());
    }

    @Test
    void testCreateTransactionInsufficientBalance() {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 1000, 5000);

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(faker.number().randomDouble(2, 100, 500));
        accountMapper.insertAccount(sourceAccount);

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber(destinationAccountNumber);
        destinationAccount.setBalance(faker.number().randomDouble(2, 1000, 5000));
        accountMapper.insertAccount(destinationAccount);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 调用方法并捕获异常
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transaction));

        // 验证结果
        Account updatedSourceAccount = accountMapper.findByAccountNumber(sourceAccountNumber);
        assertNotNull(updatedSourceAccount);
        assertEquals(sourceAccount.getBalance(), updatedSourceAccount.getBalance());

        Account updatedDestinationAccount = accountMapper.findByAccountNumber(destinationAccountNumber);
        assertNotNull(updatedDestinationAccount);
        assertEquals(destinationAccount.getBalance(), updatedDestinationAccount.getBalance());

        List<Transaction> transactions = transactionMapper.getAllTransactions();
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testCreateTransactionAccountNotFound() {
        // 准备数据
        String sourceAccountNumber = faker.number().digits(10);
        String destinationAccountNumber = faker.number().digits(10);
        double amount = faker.number().randomDouble(2, 100, 1000);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(sourceAccountNumber);
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setAmount(amount);

        // 调用方法并捕获异常
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transaction));

        // 验证结果
        List<Transaction> transactions = transactionMapper.getAllTransactions();
        assertTrue(transactions.isEmpty());
    }
}
