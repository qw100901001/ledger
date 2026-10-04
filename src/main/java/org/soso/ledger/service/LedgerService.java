package org.soso.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.soso.ledger.dto.CreateLedgerRequest;
import org.soso.ledger.entity.Ledger;
import org.soso.ledger.entity.LedgerMember;
import org.soso.ledger.mapper.LedgerMapper;
import org.soso.ledger.mapper.LedgerMemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final LedgerMapper ledgerMapper;
    private final LedgerMemberMapper ledgerMemberMapper;

    public LedgerService(LedgerMapper ledgerMapper, LedgerMemberMapper ledgerMemberMapper) {
        this.ledgerMapper = ledgerMapper;
        this.ledgerMemberMapper = ledgerMemberMapper;
    }

    /**
     * 创建账本 (事务控制)
     * 模拟场景：第一条插成功，第二条失败，@Transactional 会回滚第一条
     */
    @Transactional(rollbackFor = Exception.class)
    public Ledger createLedger(Long userId, CreateLedgerRequest req) {
        // 1. 插入账本
        Ledger ledger = new Ledger();
        ledger.setName(req.getName());
        ledger.setOwnerId(userId);
        ledger.setCreatedAt(LocalDateTime.now());
        ledgerMapper.insert(ledger);

        // 模拟异常测试事务：如果这里报错，上面的 ledgerMapper.insert 会回滚
        // if (true) throw new RuntimeException("模拟第二条插入失败");

        // 2. 插入成员关系 (当前用户是 owner)
        LedgerMember member = new LedgerMember();
        member.setLedgerId(ledger.getId());
        member.setUserId(userId);
        member.setRole("owner");
        ledgerMemberMapper.insert(member);

        return ledger;
    }

    /**
     * 我参与的账本列表 (两次查询)
     */
    public List<Ledger> getMyLedgers(Long userId) {
        // 第一步：先查成员表，拿到我参与的所有 ledgerId
        List<LedgerMember> members = ledgerMemberMapper.selectList(
                new LambdaQueryWrapper<LedgerMember>().eq(LedgerMember::getUserId, userId)
        );


        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取 ledgerId 集合
        List<Long> ledgerIds = members.stream()
                .map(LedgerMember::getLedgerId)
                .collect(Collectors.toList());

        // 第二步：用 IN 查询去查 ledgers 表
        return ledgerMapper.selectBatchIds(ledgerIds);
    }
}