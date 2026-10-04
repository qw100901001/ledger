package org.soso.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.soso.ledger.entity.Ledger;

@Mapper
public interface LedgerMapper extends BaseMapper<Ledger> {
}