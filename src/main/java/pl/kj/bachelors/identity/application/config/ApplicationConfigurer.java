package pl.kj.bachelors.identity.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.kj.bachelors.identity.application.interceptor.AuthenticationHandlerInterceptor;

@Component
public class ApplicationConfigurer implements WebMvcConfigurer {
    private final AuthenticationHandlerInterceptor authInterceptor;

    @Autowired
    public ApplicationConfigurer(
            AuthenticationHandlerInterceptor authInterceptor
    ) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authInterceptor).order(Ordered.LOWEST_PRECEDENCE);
    }
}
