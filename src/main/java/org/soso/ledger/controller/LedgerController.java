package org.soso.ledger.controller;

import jakarta.validation.Valid;
import org.soso.ledger.common.Result;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.dto.CreateLedgerRequest;
import org.soso.ledger.entity.Ledger;
import org.soso.ledger.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;
    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }
    @PostMapping
    public Result<Ledger> createLedger(@Valid @RequestBody CreateLedgerRequest req) {
        Long userId = UserContext.getCurrentUserId();
        Ledger ledger = ledgerService.createLedger(userId, req);
        return Result.success(ledger);
    }
    @GetMapping
    public Result<List<Ledger>> getMyLedgers() {
        Long userId=UserContext.getCurrentUserId();
        List<Ledger> ledgers = ledgerService.getMyLedgers(userId);
        return Result.success(ledgers);
    }

}
