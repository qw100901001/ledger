package org.soso.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.soso.ledger.dto.CreateTransactionRequest;
import org.soso.ledger.dto.TransactionUpdateRequest;
import org.soso.ledger.entity.Category;
import org.soso.ledger.entity.LedgerMember;
import org.soso.ledger.entity.Transaction;
import org.soso.ledger.exception.BusinessException;
import org.soso.ledger.mapper.CategoryMapper;
import org.soso.ledger.mapper.TransactionMapper;
import org.soso.ledger.service.LedgerMemberService;
import org.soso.ledger.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final LedgerMemberService ledgerMemberService;

    @Override
    @Transactional
    public Transaction createTransaction(Long ledgerId, Long userId,
                                         CreateTransactionRequest req) {
        // ① 成员校验：当前用户必须是这个账本的成员
        ledgerMemberService.checkMembership(ledgerId, userId);
        // ② 分类必须存在
        Category category = categoryMapper.selectById(req.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
        // ③ 分类必须属于当前账本
        if (!ledgerId.equals(category.getLedgerId())) {
            throw new BusinessException(400, "分类不属于该账本，不能跨账本记账");
        }
        // ④ type 必须和分类的 type 一致
        if (!req.getType().equals(category.getType())) {
            throw new BusinessException(400,
                    "类型不匹配：该分类是 " + category.getType()
                            + "，不能记成 " + req.getType());
        }
        Transaction tx = new Transaction();
        if (req.getRequestId() == null || req.getRequestId().isBlank()) {
            tx.setRequestId(UUID.randomUUID().toString());
        }
        tx.setLedgerId(ledgerId);
        tx.setCategoryId(req.getCategoryId());
        tx.setAmount(req.getAmount());
        tx.setType(req.getType());
        tx.setRecordTime(req.getRecordTime());
        tx.setRemark(req.getRemark());
        tx.setUserId(userId);
        transactionMapper.insert(tx);
        return tx;
    }

    @Override
    public Page<Transaction> pageTransactions(Long ledgerId, Long userId,
                                              Integer pageNum, Integer pageSize) {
        // ① 分页查询也要校验成员身份
        ledgerMemberService.checkMembership(ledgerId, userId);
        // ② 构造分页对象
        Page<Transaction> page = new Page<>(pageNum, pageSize);
        return transactionMapper.selectPage(page,
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getLedgerId, ledgerId)
                        .orderByDesc(Transaction::getRecordTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransaction(Long ledgerId, Long transactionId, Long userId, TransactionUpdateRequest request) {
        // ① 校验流水是否存在
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw new BusinessException(404, "该流水不存在");
        }
        // ② 交叉校验：流水是否属于当前账本
        if (!transaction.getLedgerId().equals(ledgerId)) {
            throw new BusinessException(403, "非法操作：该流水不属于当前账本");
        }
        // ③ 成员校验：确保用户在当前账本有权限
        ledgerMemberService.checkMembership(ledgerId, userId);

        // ④ 业务校验：如果修改了分类，必须校验新分类是否属于该账本
        if (!transaction.getCategoryId().equals(request.getCategoryId())) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null || !category.getLedgerId().equals(ledgerId)) {
                throw new BusinessException(400, "无效的分类：该分类不存在或不属于当前账本");
            }

            // (进阶可选)：如果分类有收入/支出属性，这里应检查流水类型是否匹配
            // 例如：不能把“餐饮”分类挂到一笔“收入”流水上
        }

        // ⑤ 更新字段
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setRecordTime(request.getRecordTime());
        transaction.setRemark(request.getRemark());

        // ⑥ 执行更新
        transactionMapper.updateById(transaction);

    }

    //删除流水
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long ledgerId, Long transactionId, Long userId) {
        // ③ 成员校验：确保用户在当前账本有权限
        ledgerMemberService.checkMembership(ledgerId, userId);
        // 先根据 ID 查询该笔流水是否存在
        Transaction transaction = transactionMapper.selectById(transactionId);
        // 如果流水不存在，直接抛出异常
        if (transaction == null) {
            throw new BusinessException(404,"该流水不存在或已被删除");
            // 建议替换为你项目中的自定义异常，如 BusinessException
        }
        if (!transaction.getLedgerId().equals(ledgerId)) {
            throw new BusinessException(403, "非法操作：该流水不属于当前账本");
        }
        // 执行真删除（物理删除）
        int rows = transactionMapper.deleteById(transactionId);
        if (rows == 0) {
            throw new BusinessException(400,"删除失败");
        }
    }

}
