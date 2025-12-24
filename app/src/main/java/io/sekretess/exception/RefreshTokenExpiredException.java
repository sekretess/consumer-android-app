package io.sekretess.exception;

public class RefreshTokenExpiredException extends Exception {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
