package com.pkg.balance.mgmt.controller;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.service.AccountService;
import com.pkg.balance.mgmt.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping(path = "/get")
    public Account getAccount(@RequestParam(name = "accountNumber") String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @PostMapping("/create")
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @PostMapping("/increaseBalance")
    public void increaseBalance(@RequestBody Account account) {
        accountService.increaseBalance(account);
    }

    @PostMapping("/decreaseBalance")
    public void decreaseBalance(@RequestBody Account account) {
        accountService.decreaseBalance(account);
    }

    @PostMapping("/createTransaction")
    public void createTransaction(@RequestBody Transaction transaction) {
        transactionService.createTransaction(transaction);
    }

    @PostMapping("/updateBalance")
    public void updateBalance(@RequestBody Account account) {
        accountService.updateBalance(account);
    }
}
