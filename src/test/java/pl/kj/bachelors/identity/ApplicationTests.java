package pl.kj.bachelors.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.kj.bachelors.identity.application.Application;

@SpringBootTest
@ContextConfiguration(classes = { Application.class })
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
