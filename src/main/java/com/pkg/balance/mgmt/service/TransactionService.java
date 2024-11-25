package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "account_lock_";

    @Transactional
    public void createTransaction(Transaction transaction) {
        String sourceLockKey = LOCK_PREFIX + transaction.getSourceAccountNumber();
        String destinationLockKey = LOCK_PREFIX + transaction.getDestinationAccountNumber();

        RLock sourceLock = redissonClient.getLock(sourceLockKey);
        RLock destinationLock = redissonClient.getLock(destinationLockKey);

        try {
            // 尝试获取分布式锁，最多等待10秒
            boolean isSourceLocked = sourceLock.tryLock(10, TimeUnit.SECONDS);
            boolean isDestinationLocked = destinationLock.tryLock(10, TimeUnit.SECONDS);

            if (!isSourceLocked || !isDestinationLocked) {
                throw new RuntimeException("Failed to acquire lock for accounts: " + transaction.getSourceAccountNumber() + " or " + transaction.getDestinationAccountNumber());
            }

            // 扣减源账户余额
            Account sourceAccount = accountMapper.findByAccountNumber(transaction.getSourceAccountNumber());
            if (sourceAccount == null) {
                throw new RuntimeException("Source account not found");
            }

            double newSourceBalance = sourceAccount.getBalance() - transaction.getAmount();
            if (newSourceBalance < 0) {
                throw new RuntimeException("Insufficient balance in source account");
            }

            sourceAccount.setBalance(newSourceBalance);
            accountMapper.updateAccount(sourceAccount);

            // 增加目标账户余额
            Account destinationAccount = accountMapper.findByAccountNumber(transaction.getDestinationAccountNumber());
            if (destinationAccount == null) {
                throw new RuntimeException("Destination account not found");
            }

            double newDestinationBalance = destinationAccount.getBalance() + transaction.getAmount();
            destinationAccount.setBalance(newDestinationBalance);
            accountMapper.updateAccount(destinationAccount);

            // 设置唯一交易ID
            transaction.setTransactionId(UUID.randomUUID().toString());

            // 创建交易记录
            transactionMapper.insertTransaction(transaction);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed", e);
        } finally {
            // 释放分布式锁
            if (sourceLock.isHeldByCurrentThread()) {
                sourceLock.unlock();
            }
            if (destinationLock.isHeldByCurrentThread()) {
                destinationLock.unlock();
            }
        }
    }
}
