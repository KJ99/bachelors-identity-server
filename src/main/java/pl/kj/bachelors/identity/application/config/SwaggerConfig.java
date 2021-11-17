package pl.kj.bachelors.identity.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kj.bachelors.identity.domain.config.JwtConfig;

@Configuration
public class SwaggerConfig {
    private final JwtConfig jwtConfig;
    private final AppConfig appConfig;

    @Autowired
    public SwaggerConfig(JwtConfig jwtConfig, AppConfig appConfig) {
        this.jwtConfig = jwtConfig;
        this.appConfig = appConfig;
    }

    @Bean
    public OpenAPI openApi() {
        Components swaggerComponents = new Components();
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(this.jwtConfig.getType())
                .bearerFormat("JWT");

        swaggerComponents
                .addSecuritySchemes("JWT", securityScheme);

        Info apiInfo = new Info()
                .title(this.appConfig.getName())
                .description(this.appConfig.getDescription())
                .version(this.appConfig.getVersion());

        return new OpenAPI()
                .components(swaggerComponents)
                .info(apiInfo);
    }
}
