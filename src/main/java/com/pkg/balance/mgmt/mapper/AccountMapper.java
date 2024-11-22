package com.pkg.balance.mgmt.mapper;

import com.pkg.balance.mgmt.entity.Account;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {

    @Select("SELECT * FROM account WHERE account_number = #{accountNumber}")
    Account findByAccountNumber(String accountNumber);

    @Update("UPDATE account SET balance = #{balance} WHERE account_number = #{accountNumber}")
    int updateBalance(Account account);
}
