package com.pkg.balance.mgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.service.AccountService;
import com.pkg.balance.mgmt.service.TransactionService;
import com.pkg.balance.mgmt.BalanceMgmtApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@SpringBootTest(classes = BalanceMgmtApplication.class)
@ActiveProfiles("test")
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetAccount() throws Exception {
        String accountNumber = "123456";
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(100.0);

        Mockito.when(accountService.getAccountByNumber(accountNumber)).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/account/get?accountNumber=123456", accountNumber))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(account)));
    }

    @Test
    public void testCreateAccount() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(100.0);

        Mockito.when(accountService.createAccount(account)).thenReturn(account);

//        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(account)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(account)));
    }

    @Test
    public void testIncreaseBalance() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(100.0);

        Mockito.doNothing().when(accountService).increaseBalance(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/increaseBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testDecreaseBalance() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(100.0);

        Mockito.doNothing().when(accountService).decreaseBalance(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/decreaseBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testCreateTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setAmount(50.0);

        Mockito.doNothing().when(transactionService).createTransaction(transaction);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/createTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUpdateBalance() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(100.0);

        Mockito.doNothing().when(accountService).updateBalance(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/updateBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
