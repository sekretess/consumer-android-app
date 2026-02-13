package io.sekretess.exception;

public class UnAuhthorizedException extends Exception {
    public UnAuhthorizedException(String message) {
        super(message);
    }

    public UnAuhthorizedException(String message, Throwable t){
        super(message, t);
    }
}
