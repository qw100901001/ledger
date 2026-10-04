package org.soso.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.soso.ledger.dto.CategoryDTO;
import org.soso.ledger.dto.CreateCategoryRequest;
import org.soso.ledger.dto.CategoryUpdateRequest;
import org.soso.ledger.entity.Category;
import org.soso.ledger.exception.BusinessException;
import org.soso.ledger.mapper.CategoryMapper;
import org.soso.ledger.service.CategoryService;
import org.soso.ledger.service.LedgerMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final LedgerMemberService ledgerMemberService;

    @Override
    public Category createCategory(Long ledgerId, Long userId, CreateCategoryRequest req) {
        // ① 成员校验：当前用户必须是这个账本的成员
        ledgerMemberService.checkMembership(ledgerId, userId);

        // ② 同一账本下，分类名不能重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getLedgerId, ledgerId)
                        .eq(Category::getName, req.getName()));
        if (count > 0) {
            throw new BusinessException(409, "该账本下已存在同名分类");
        }

        // ③ 组装并落库
        Category c = new Category();
        c.setLedgerId(ledgerId);
        c.setName(req.getName());
        c.setType(req.getType());
        c.setIcon(req.getIcon());
        c.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        categoryMapper.insert(c);
        return c;
    }

    @Override
    public List<CategoryDTO> listByLedger(Long ledgerId, Long userId) {
        // ① 成员校验：不能随便翻别人账本的分类
        ledgerMemberService.checkMembership(ledgerId, userId);

        // ② 查询并按 type + sortOrder 排序
        List<Category> list = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getLedgerId, ledgerId)
                        .orderByAsc(Category::getType)
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId));

        // ③ 转成 DTO 返回，避免暴露 ledgerId
        return list.stream().map(c -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setType(c.getType());
            dto.setIcon(c.getIcon());
            dto.setSortOrder(c.getSortOrder());
            return dto;
        }).collect(Collectors.toList());
    }

    //更新
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long ledgerId, Long userId,Long categoryId,  CategoryUpdateRequest request) {
        // ① 查分类是否存在
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "该分类不存在");
        }
        // ② 关键校验：确保这个分类真的属于路径里传的这个账本！
        if (!category.getLedgerId().equals(ledgerId)) {
            throw new BusinessException(500, "非法操作：该分类不属于当前账本");
        }
        // ③ 成员校验：确保当前用户在这个账本里有权限
        ledgerMemberService.checkMembership(ledgerId, userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setType(request.getType());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        categoryMapper.updateById(category);
    }
}