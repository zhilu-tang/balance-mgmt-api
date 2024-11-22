package com.pkg.balance.mgmt.service;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    public void setUp() {
        testAccount = new Account();
        testAccount.setAccountNumber("123456");
        testAccount.setBalance(1000.0);
    }

    @Test
    public void testGetAccountByNumber() {
        when(accountMapper.findByAccountNumber(testAccount.getAccountNumber())).thenReturn(testAccount);

        Account result = accountService.getAccountByNumber(testAccount.getAccountNumber());

        assertNotNull(result);
        assertEquals(testAccount, result);
        verify(accountMapper).findByAccountNumber(testAccount.getAccountNumber());
    }

    @Test
    public void testCreateAccount() {
        when(accountMapper.insertAccount(testAccount)).thenReturn(1);

        Account createdAccount = accountService.createAccount(testAccount);

        assertNotNull(createdAccount);
        assertEquals(testAccount, createdAccount);
        verify(accountMapper).insertAccount(testAccount);
    }

    @Test
    public void testIncreaseBalance() {
        when(accountMapper.increaseBalance(testAccount)).thenReturn(1);

        accountService.increaseBalance(testAccount);

        verify(accountMapper).increaseBalance(testAccount);
    }

    @Test
    public void testDecreaseBalance() {
        when(accountMapper.decreaseBalance(testAccount)).thenReturn(1);

        accountService.decreaseBalance(testAccount);

        verify(accountMapper).decreaseBalance(testAccount);
    }

    @Test
    public void testUpdateBalance() {
        when(accountMapper.updateBalance(testAccount)).thenReturn(1);

        accountService.updateBalance(testAccount);

        verify(accountMapper).updateBalance(testAccount);
    }
}
