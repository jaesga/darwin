package models.validation;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = PasswordPolicy.class)
public @interface Password {
    String message() default PasswordPolicy.message;
    boolean checkUsername() default true;
    String usernameField() default "email";
}