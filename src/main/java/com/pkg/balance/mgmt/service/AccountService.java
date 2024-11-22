package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    public Account getAccountByNumber(String accountNumber) {
        return accountMapper.findByAccountNumber(accountNumber);
    }

    public Account createAccount(Account account) {
        accountMapper.insertAccount(account);
        return account;
    }

    public void increaseBalance(Account account) {
        accountMapper.increaseBalance(account);
    }

    public void decreaseBalance(Account account) {
        accountMapper.decreaseBalance(account);
    }

    public void updateBalance(Account account) {
        accountMapper.updateBalance(account);
    }
}
