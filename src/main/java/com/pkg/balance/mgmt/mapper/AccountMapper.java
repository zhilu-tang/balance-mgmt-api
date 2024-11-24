package com.pkg.balance.mgmt.mapper;

import com.pkg.balance.mgmt.entity.Account;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {

    @Select("SELECT * FROM account WHERE account_number = #{accountNumber}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "accountNumber", column = "account_number"),
            @Result(property = "balance", column = "balance")
    })
    Account findByAccountNumber(String accountNumber);

    @Insert("INSERT INTO account (account_number, balance) VALUES (#{accountNumber}, #{balance})")
    int insertAccount(Account account);

    @Update("UPDATE account SET balance = balance + #{balance} WHERE account_number = #{accountNumber}")
    int increaseBalance(Account account);

    @Update("UPDATE account SET balance = balance - #{balance} WHERE account_number = #{accountNumber}")
    int decreaseBalance(Account account);

    @Update("UPDATE account SET balance = #{balance} WHERE account_number = #{accountNumber}")
    int updateBalance(Account account);

    @Select("SELECT * FROM account WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "accountNumber", column = "account_number"),
            @Result(property = "balance", column = "balance")
    })
    Account getAccountById(int id);
    @Update("UPDATE account SET balance = #{balance}, account_number = #{accountNumber} WHERE id = #{id}")
    int updateAccount(Account account);
}
