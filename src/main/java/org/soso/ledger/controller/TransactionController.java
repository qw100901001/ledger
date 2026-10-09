package org.soso.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.soso.ledger.common.Result;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.dto.CreateTransactionRequest;
import org.soso.ledger.dto.TransactionUpdateRequest;
import org.soso.ledger.entity.Transaction;
import org.soso.ledger.service.TransactionService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 记一笔交易。
     * POST /ledgers/{ledgerId}/transactions
     */
    @PostMapping("/ledgers/{ledgerId}/transactions")
    public Result<Transaction> createTransaction(
            @PathVariable Long ledgerId,
            @Valid @RequestBody CreateTransactionRequest req) {

        Long userId = UserContext.getCurrentUserId();
        Transaction tx = transactionService.createTransaction(ledgerId, userId, req);
        return Result.success(tx);
    }

    /**
     * 分页查询交易列表。
     * GET /ledgers/{ledgerId}/transactions?pageNum=1&pageSize=10
     */
    @GetMapping("/ledgers/{ledgerId}/transactions")
    public Result<Page<Transaction>> pageTransactions(
            @PathVariable Long ledgerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = UserContext.getCurrentUserId();
        Page<Transaction> page = transactionService
                .pageTransactions(ledgerId, userId, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/transactions/{ledgerId}/categories/{transactionId}")
    public Result<Void> updateTransaction(
            @PathVariable Long ledgerId,
            @PathVariable Long transactionId,
            @Valid @RequestBody TransactionUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        transactionService.updateTransaction(ledgerId, transactionId, userId, request);
        return Result.success(null);
    }

    @DeleteMapping("/transactions/{ledgerId}/transactions/{transactionId}")
    public Result<Void> updateTransaction(
            @PathVariable Long ledgerId,
            @PathVariable Long transactionId) {
        Long userId = UserContext.getCurrentUserId();
        transactionService.deleteTransaction(ledgerId, transactionId, userId);
        return Result.success(null);
    }

}
