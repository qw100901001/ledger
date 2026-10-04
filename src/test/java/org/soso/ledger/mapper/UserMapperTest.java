package org.soso.ledger.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.soso.ledger.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional   // 测试结束自动回滚：自己插的数据自己带走，不污染库
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testSelectByUsername() {
        // Arrange：自己造数据，不依赖库里恰好有谁
        String username = "test_" + System.currentTimeMillis();
        User seed = new User();
        seed.setUsername(username);
        seed.setPasswordHash("dummy-hash");
        userMapper.insert(seed);

        // Act：查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // Assert：断言
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }
}
