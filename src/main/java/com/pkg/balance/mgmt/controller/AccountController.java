package com.pkg.balance.mgmt.controller;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{accountNumber}")
    public Account getAccount(@PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @PostMapping("/updateBalance")
    public void updateBalance(@RequestBody Account account) {
        accountService.updateBalance(account);
    }
}
