package models.exception;

public class PasswordConstraintViolationException extends Exception {

    public PasswordConstraintViolationException(String message) {
        super(message);
    }

}
