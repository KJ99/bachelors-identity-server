package pl.kj.bachelors.identity.domain.model;

import pl.kj.bachelors.identity.domain.model.payload.AuthPayload;

public class AuthResult<T extends AuthPayload> {
    private AuthResultDetail detail;
    private T payload;

    public AuthResultDetail getDetail() {
        return detail;
    }

    public void setDetail(AuthResultDetail detail) {
        this.detail = detail;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
