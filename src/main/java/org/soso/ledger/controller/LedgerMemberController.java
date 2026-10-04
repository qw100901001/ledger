package org.soso.ledger.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.soso.ledger.common.Result;
import org.soso.ledger.dto.InviteMemberRequest;
import org.soso.ledger.dto.LedgerMemberDTO;
import org.soso.ledger.entity.User;
import org.soso.ledger.service.LedgerMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ledgers/{ledgerId}/members")
@RequiredArgsConstructor
public class LedgerMemberController {

    private final LedgerMemberService ledgerMemberService;

    /**
     * GET /ledgers/{id}/members 成员列表
     */
    @GetMapping
    public Result<List<LedgerMemberDTO>> getMembers(@PathVariable Long ledgerId) {
        List<LedgerMemberDTO> members = ledgerMemberService.getMembers(ledgerId);
        return Result.success(members);
    }

    /**
     * POST /ledgers/{id}/members 拉人
     */
    @PostMapping
    public Result<Void> inviteMember(
            @PathVariable Long ledgerId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        ledgerMemberService.inviteMember(ledgerId, request);
        return Result.<Void>success(null);
    }
}