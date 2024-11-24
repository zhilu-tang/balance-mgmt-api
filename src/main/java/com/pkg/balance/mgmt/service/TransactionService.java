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
        String lockKey = LOCK_PREFIX + transaction.getAccountNumber();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取分布式锁
            lock.lock(10, TimeUnit.SECONDS);

            // 扣减账户余额
            Account account = accountMapper.findByAccountNumber(transaction.getAccountNumber());
            if (account == null) {
                throw new RuntimeException("Account not found");
            }

            double newBalance = account.getBalance() - transaction.getAmount();
            if (newBalance < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            account.setBalance(newBalance);
            accountMapper.updateAccount(account);

            // 创建交易记录
            transactionMapper.insertTransaction(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed", e);
        } finally {
            // 释放分布式锁
            lock.unlock();
        }
    }
}
