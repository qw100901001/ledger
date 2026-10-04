package org.soso.ledger;

import org.junit.jupiter.api.Test;
import org.soso.ledger.entity.User;
import org.soso.ledger.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional   // 核心：方法执行完自动回滚，不污染数据库
class LedgerApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Test
	void testSelectById_ShouldReturnUser_WhenUserExists() {
		// 1. 构造测试数据
		String testUsername = "test_" + System.currentTimeMillis(); // 防止撞名
		User testUser = new User();
		testUser.setUsername(testUsername);
		testUser.setPasswordHash("123456"); // 注意：如果是真实业务，这里最好加密
		testUser.setNickname(testUsername);
		// 2. 插入测试数据
		int insertCount=userMapper.insert(testUser);
		assertEquals(1, insertCount);
		// MyBatis-Plus 插入后，会自动将生成的 ID 回填到 testUser 对象中
		Long generatedId = testUser.getId();
		assertNotNull(generatedId);
		// 3. 执行查询
		User queriedUser = userMapper.selectById(generatedId);
		// 4. 断言
		assertNotNull(queriedUser);
		assertEquals(testUsername, queriedUser.getUsername());

	}

}
