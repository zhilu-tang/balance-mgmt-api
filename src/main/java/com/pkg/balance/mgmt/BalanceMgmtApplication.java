package com.pkg.balance.mgmt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@MapperScan("com.pkg.balance.mgmt.mapper")
@SpringBootApplication
public class BalanceMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(BalanceMgmtApplication.class, args);
    }
}
