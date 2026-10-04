package org.soso.ledger.controller;

import org.soso.ledger.dto.CreateCategoryRequest;
import org.soso.ledger.dto.CategoryUpdateRequest;
import org.soso.ledger.service.CategoryService;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.common.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.soso.ledger.dto.CategoryDTO;

import java.util.List;

@RestController
@RequestMapping("/ledgers/{ledgerId}/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<Void> createCategory(
            @PathVariable Long ledgerId,
            @Valid @RequestBody CreateCategoryRequest request) {
        Long userId = UserContext.getCurrentUserId();

        // 调用 Service 层处理业务逻辑
        categoryService.createCategory(ledgerId, userId, request);

        return Result.success(null);
    }

    @GetMapping
    public Result<List<CategoryDTO>> getCategories(@PathVariable Long ledgerId) {
        Long userId = UserContext.getCurrentUserId();
        List<CategoryDTO> categories = categoryService.listByLedger(ledgerId, userId);
        return Result.success(categories);
    }

    @PutMapping("/{categoryId}")
    public Result<Void> updateCategory(
            @PathVariable Long ledgerId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        categoryService.updateCategory(ledgerId, userId, categoryId, request);
        return Result.success(null);

    }


}
