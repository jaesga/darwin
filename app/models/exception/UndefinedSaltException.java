package models.exception;

public class UndefinedSaltException extends RuntimeException {

    public UndefinedSaltException(String message) {
        super(message);
    }
}
