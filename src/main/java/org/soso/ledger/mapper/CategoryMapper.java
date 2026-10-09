package org.soso.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import org.soso.ledger.entity.Category;


@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    /**
     * 悲观锁查询分类
     * 注意：必须在事务中调用，否则 FOR UPDATE 会立即失效
     */
    @Select("SELECT * FROM category WHERE id = #{id} AND ledger_id = #{ledgerId} FOR UPDATE")
    Category selectByIdForUpdate(@Param("id") Long id, @Param("ledgerId") Long ledgerId);
}
