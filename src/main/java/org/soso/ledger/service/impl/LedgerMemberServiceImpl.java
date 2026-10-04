package org.soso.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.dto.InviteMemberRequest;
import org.soso.ledger.dto.LedgerMemberDTO;
import org.soso.ledger.entity.Ledger;
import org.soso.ledger.entity.LedgerMember;
import org.soso.ledger.entity.User;
import org.soso.ledger.exception.BusinessException;
import org.soso.ledger.mapper.LedgerMapper;
import org.soso.ledger.mapper.LedgerMemberMapper;
import org.soso.ledger.mapper.UserMapper;
import org.soso.ledger.service.LedgerMemberService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerMemberServiceImpl implements LedgerMemberService {

    private final LedgerMemberMapper ledgerMemberMapper;
    private final UserMapper userMapper;
    private final LedgerMapper ledgerMapper; // 新增，用于查账本信息

    // 抽取公共校验方法
    @Override
    public void checkMembership(Long ledgerId, Long userId) {
        Long count = ledgerMemberMapper.selectCount(
                new LambdaQueryWrapper<LedgerMember>()
                        .eq(LedgerMember::getLedgerId, ledgerId)
                        .eq(LedgerMember::getUserId, userId)
        );
        if (count == 0) {
            throw new BusinessException(403, "你不是该账本的成员，无权操作");
        }
    }

    @Override
    public List<LedgerMemberDTO> getMembers(Long ledgerId) {
        // 1. 前置校验：你是不是这个账本的人？
        Long currentUserId = UserContext.getCurrentUserId();
        checkMembership(ledgerId, currentUserId);

        // 2. 查账本的所有成员关系
        List<LedgerMember> members = ledgerMemberMapper.selectList(
                new LambdaQueryWrapper<LedgerMember>().eq(LedgerMember::getLedgerId, ledgerId)
        );
        if (members.isEmpty()) {
            return List.of();
        }

        // 3. 提取 userId 集合，查用户信息并返回
        List<Long> userIds = members.stream().map(LedgerMember::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().map(user->{
            LedgerMemberDTO dto = new LedgerMemberDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void inviteMember(Long ledgerId, InviteMemberRequest request) {
        Long currentUserId = UserContext.getCurrentUserId();

        // 1. 校验：只有 owner 能拉人
        Ledger ledger = ledgerMapper.selectById(ledgerId);
        if (ledger == null) {
            throw new BusinessException(404, "账本不存在");
        }
        if (!ledger.getOwnerId().equals(currentUserId)) {
            throw new BusinessException(403, "只有账本所有者才能邀请成员");
        }

        // 2. 校验：被拉的人必须是已注册用户
        User targetUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (targetUser == null) {
            throw new BusinessException(404, "该用户尚未注册");
        }

        // 3. 校验：是否重复拉人
        Long count = ledgerMemberMapper.selectCount(
                new LambdaQueryWrapper<LedgerMember>()
                        .eq(LedgerMember::getLedgerId, ledgerId)
                        .eq(LedgerMember::getUserId, targetUser.getId())
        );
        if (count > 0) {
            throw new BusinessException(409, "该用户已经是账本成员，请勿重复邀请");
        }

        // 4. 插入成员关系
        LedgerMember newMember = new LedgerMember();
        newMember.setLedgerId(ledgerId);
        newMember.setUserId(targetUser.getId());
        newMember.setRole("member"); // 设置默认角色
        ledgerMemberMapper.insert(newMember);
    }
}