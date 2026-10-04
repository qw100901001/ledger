package org.soso.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.soso.ledger.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}