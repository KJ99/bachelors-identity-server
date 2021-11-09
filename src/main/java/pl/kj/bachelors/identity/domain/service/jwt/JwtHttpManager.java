package pl.kj.bachelors.identity.domain.service.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface JwtHttpManager {
    String getFromRequest(HttpServletRequest request);
    void putInResponse(String token, HttpServletResponse response);
}
