package org.soso.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.soso.ledger.dto.CreateTransactionRequest;
import org.soso.ledger.dto.TransactionUpdateRequest;
import org.soso.ledger.entity.Transaction;

public interface TransactionService {
    /**
     * 在指定账本下记一笔交易。
     */
    Transaction createTransaction(Long ledgerId, Long userId, CreateTransactionRequest req);

    /**
     * 分页查询某账本下的交易记录。
     */
    Page<Transaction> pageTransactions(Long ledgerId, Long userId,
                                       Integer pageNum, Integer pageSize);

    void updateTransaction(Long ledgerId, Long transactionId, Long userId, TransactionUpdateRequest request);

    void deleteTransaction(Long ledgerId, Long transactionId, Long userId);

}