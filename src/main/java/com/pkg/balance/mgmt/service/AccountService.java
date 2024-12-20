package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import com.pkg.balance.mgmt.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling account-related operations.
 */
@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    public Account getAccountByNumber(String accountNumber) {
        return accountMapper.findByAccountNumber(accountNumber);
    }

    public Account createAccount(Account account) {
        Account byAccountNumber = accountMapper.findByAccountNumber(account.getAccountNumber());
        if(byAccountNumber != null) {
            return byAccountNumber;
        }
        accountMapper.insertAccount(account);
        return account;
    }
}
