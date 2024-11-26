package com.pkg.balance.mgmt.controller;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.service.AccountService;
import com.pkg.balance.mgmt.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class responsible for handling HTTP requests related to accounts and transactions.
 */
@RestController
@RequestMapping("/api/account")
@Slf4j
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

    @PostMapping("/createTransaction")
    public void createTransaction(@RequestBody Transaction transaction) {
        log.info("Transaction: {}", transaction);
        transactionService.createTransaction(transaction);
    }

}
