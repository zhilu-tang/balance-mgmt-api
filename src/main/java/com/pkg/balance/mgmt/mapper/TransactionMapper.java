package com.pkg.balance.mgmt.mapper;

import com.pkg.balance.mgmt.entity.Transaction;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transaction (transaction_id, source_account_number, destination_account_number, amount)" +
            "        VALUES (#{transactionId}, #{sourceAccountNumber}, #{destinationAccountNumber}, #{amount})")
    int insertTransaction(Transaction transaction);

    @Delete("DELETE FROM transaction")
    void deleteAllTransactions();

    @Select("SELECT * FROM transaction")
    @Results({
            @Result(property = "transactionId", column = "transaction_id"),
            @Result(property = "sourceAccountNumber", column = "source_account_number"),
            @Result(property = "destinationAccountNumber", column = "destination_account_number"),
            @Result(property = "amount", column = "amount")
    })
    List<Transaction> getAllTransactions();
}
