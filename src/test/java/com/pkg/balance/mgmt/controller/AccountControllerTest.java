package com.pkg.balance.mgmt.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pkg.balance.mgmt.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.pkg.balance.mgmt.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    public void testGetBalance() throws Exception {
        // 假设 getBalance 返回 100.0
        when(accountService.getAccountByNumber("1")).thenReturn(
                new Account(1L, "1", 100.0)
        );

        mockMvc.perform(get("/api/account/1"))
               .andExpect(status().isOk())
               .andExpect(content().string("100.0"));

        verify(accountService, times(1)).getAccountByNumber("1");
    }
}
