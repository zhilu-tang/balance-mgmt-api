package com.pkg.balance.mgmt.integration;

import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import com.pkg.balance.mgmt.service.AccountService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AccountControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountService accountService;

    private Faker faker;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        // 清理或初始化数据
    }

    @Test
    public void testGetAccount() {
        // 创建一个账户
        Account account = new Account();
        account.setAccountNumber(faker.number().digits(6));
        account.setBalance(faker.number().randomDouble(2, 1000, 10000));
        accountService.createAccount(account);

        // 发送 GET 请求
        ResponseEntity<Account> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/account/get?accountNumber=" + account.getAccountNumber(),
                Account.class
        );

        // 验证响应
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(account.getAccountNumber(), response.getBody().getAccountNumber());
        assertEquals(account.getBalance(), response.getBody().getBalance());
    }

    @Test
    public void testCreateAccount() {
        // 准备请求体
        Account account = new Account();
        account.setAccountNumber(faker.number().digits(6));
        account.setBalance(faker.number().randomDouble(2, 1000, 10000));

        // 发送 POST 请求
        ResponseEntity<Account> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/create",
                account,
                Account.class
        );

        // 验证响应
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(account.getAccountNumber(), response.getBody().getAccountNumber());
        assertEquals(account.getBalance(), response.getBody().getBalance());
    }

    @Test
    public void testCreateTransaction() {
        // 创建一个账户
        Account account = new Account();
        account.setAccountNumber(faker.number().digits(6));
        account.setBalance(faker.number().randomDouble(2, 1000, 10000));
        accountService.createAccount(account);

        // 创建交易
        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(account.getAccountNumber());
        transaction.setAmount(faker.number().randomDouble(2, 100, 1000));

        // 发送 POST 请求
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/account/createTransaction",
                transaction,
                Void.class
        );

        // 验证交易创建
        ResponseEntity<Account> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/account/get?accountNumber=" + account.getAccountNumber(),
                Account.class
        );

        assertEquals(200, response.getStatusCodeValue());
        // 这里可以根据业务逻辑进一步验证交易是否成功
    }
}
