package pl.kj.bachelors.identity.unit.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.infrastructure.service.TokenGenerationService;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenGenerationServiceTests extends BaseTest {
    @Autowired
    private TokenGenerationService service;

    @Test
    public void testGenerateToken() throws ExecutionException, InterruptedException {
        String token = this.service.generateToken("pre", "suf", 10);
        assertThat(token).isNotNull().isNotEmpty();
    }
}
