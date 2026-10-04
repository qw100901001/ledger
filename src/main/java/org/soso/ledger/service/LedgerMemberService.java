package org.soso.ledger.service;

import jakarta.validation.Valid;
import org.soso.ledger.dto.InviteMemberRequest;
import org.soso.ledger.dto.LedgerMemberDTO;
import org.soso.ledger.entity.User;
import java.util.List;

public interface LedgerMemberService {
    // 查询成员列表（已有）
    List<LedgerMemberDTO> getMembers(Long ledgerId);

    void checkMembership(Long ledgerId, Long userId);

    // 拉人
    void inviteMember(Long ledgerId, InviteMemberRequest request);

}

