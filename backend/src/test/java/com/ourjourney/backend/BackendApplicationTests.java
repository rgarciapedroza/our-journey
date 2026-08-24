package com.ourjourney.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-jwt-authentication-tests-123456789"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}
