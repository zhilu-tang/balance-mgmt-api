package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    public Account getAccountByNumber(String accountNumber) {
        return accountMapper.findByAccountNumber(accountNumber);
    }

    public void updateBalance(Account account) {
        accountMapper.updateBalance(account);
    }
}
