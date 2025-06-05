package com.example.contacts;

import com.example.contacts.config.CustomTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(CustomTestConfiguration.class)
@ActiveProfiles("test")
class ContactsApplicationTests {

	@Test
	void contextLoads() {
	}

}
