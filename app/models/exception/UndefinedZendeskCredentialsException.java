package models.exception;

public class UndefinedZendeskCredentialsException extends RuntimeException {

    public UndefinedZendeskCredentialsException(String message) {
        super(message);
    }
}
