package pl.kj.bachelors.identity.application.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
        logger.info(
                String.format(
                        "%s %s was called from address %s",
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getRemoteAddr()
                )
        );

        filterChain.doFilter(request, response);

        logger.info(
                String.format(
                        "Request %s %s from address %s was responded with code %s",
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        response.getStatus()
                )
        );
    }
}
