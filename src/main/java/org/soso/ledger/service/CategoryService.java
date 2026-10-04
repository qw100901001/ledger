package org.soso.ledger.service;

import org.soso.ledger.dto.CategoryDTO;
import org.soso.ledger.dto.CreateCategoryRequest;
import org.soso.ledger.dto.CategoryUpdateRequest;
import org.soso.ledger.entity.Category;

import java.util.List;

public interface CategoryService {

    /** 创建分类 */
    Category  createCategory(Long ledgerId, Long userId, CreateCategoryRequest req);
    /** 更新分类*/
    void updateCategory(Long ledgerId, Long userId, Long categoryId,CategoryUpdateRequest req);

    /** 查询某账本下的所有分类 */
    List<CategoryDTO> listByLedger(Long ledgerId, Long userId);
}