package com.pkg.balance.mgmt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.namesrv.addr}")
    private String namesrvAddr;

    private static final String LOCK_PREFIX = "account_lock_";
    private DefaultMQProducer producer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(namesrvAddr);
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("Failed to start RocketMQ producer", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    @Transactional
    public void createTransaction(Transaction transaction) {
        String sourceLockKey = LOCK_PREFIX + transaction.getSourceAccountNumber();
        String destinationLockKey = LOCK_PREFIX + transaction.getDestinationAccountNumber();

        // 确保总是先获取账户编号较小的锁
        if (transaction.getSourceAccountNumber().compareTo(transaction.getDestinationAccountNumber()) < 0) {
            lockAndProcessTransaction(sourceLockKey, destinationLockKey, transaction);
        } else {
            lockAndProcessTransaction(destinationLockKey, sourceLockKey, transaction);
        }
    }

    private void lockAndProcessTransaction(String firstLockKey, String secondLockKey, Transaction transaction) {
        RLock firstLock = redissonClient.getLock(firstLockKey);
        RLock secondLock = redissonClient.getLock(secondLockKey);

        try {
            // 尝试获取分布式锁，最多等待10秒
            boolean isFirstLocked = firstLock.tryLock(10, TimeUnit.SECONDS);
            boolean isSecondLocked = secondLock.tryLock(10, TimeUnit.SECONDS);

            if (!isFirstLocked || !isSecondLocked) {
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
            sendToRetryQueue(transaction);
            throw new RuntimeException("Transaction interrupted", e);
        } catch (Exception e) {
            sendToRetryQueue(transaction);
            throw new RuntimeException("Transaction failed", e);
        } finally {
            // 释放分布式锁
            if (firstLock.isHeldByCurrentThread()) {
                firstLock.unlock();
            }
            if (secondLock.isHeldByCurrentThread()) {
                secondLock.unlock();
            }
        }
    }

    void sendToRetryQueue(Transaction transaction) {
        try {
            String messageBody = objectMapper.writeValueAsString(transaction);
            Message message = new Message("TransactionRetryTopic", "TagA", messageBody.getBytes());
            SendResult sendResult = producer.send(message);
            if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                throw new RuntimeException("Failed to send transaction to retry queue: " + sendResult.getSendStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send transaction to retry queue", e);
        }
    }
}
