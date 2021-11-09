package pl.kj.bachelors.identity.domain.model.payload;

import pl.kj.bachelors.identity.domain.model.entity.User;

public class PasswordAuthPayload extends AuthPayload {
    private TokenAuthPayload token;
    private User user;

    public TokenAuthPayload getToken() {
        return token;
    }

    public void setToken(TokenAuthPayload token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
