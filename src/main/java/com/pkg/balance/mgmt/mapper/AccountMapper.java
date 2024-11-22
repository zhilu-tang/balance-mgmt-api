package com.pkg.balance.mgmt.mapper;

import com.pkg.balance.mgmt.entity.Account;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {

    @Select("SELECT * FROM account WHERE account_number = #{accountNumber}")
    Account findByAccountNumber(String accountNumber);

    @Insert("INSERT INTO account (account_number, balance) VALUES (#{accountNumber}, #{balance})")
    int insertAccount(Account account);

    @Update("UPDATE account SET balance = balance + #{balance} WHERE account_number = #{accountNumber}")
    int increaseBalance(Account account);

    @Update("UPDATE account SET balance = balance - #{balance} WHERE account_number = #{accountNumber}")
    int decreaseBalance(Account account);

    @Update("UPDATE account SET balance = #{balance} WHERE account_number = #{accountNumber}")
    int updateBalance(Account account);
}
