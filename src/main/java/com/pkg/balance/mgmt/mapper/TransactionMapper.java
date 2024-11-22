package com.pkg.balance.mgmt.mapper;

import com.pkg.balance.mgmt.entity.Transaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transaction (account_number, amount, type) VALUES (#{accountNumber}, #{amount}, #{type})")
    int insertTransaction(Transaction transaction);
}
