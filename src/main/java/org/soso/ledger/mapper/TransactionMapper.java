package org.soso.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.soso.ledger.entity.Transaction;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}