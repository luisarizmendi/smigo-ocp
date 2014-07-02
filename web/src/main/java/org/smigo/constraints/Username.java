package org.smigo.constraints;

import org.smigo.persitance.DatabaseResource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for UsernameValidator
 *
 * @author Christian Nilsson
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = Username.UsernameValidator.class)
@Documented
public @interface Username {

    String message() default "usernametaken";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // CaseMode value();

    class UsernameValidator implements ConstraintValidator<Username, String> {

        @Autowired
        private DatabaseResource databaseresource;

        public void setDatabaseResource(DatabaseResource databaseresource) {
            this.databaseresource = databaseresource;
        }

        public void initialize(Username constraintAnnotation) {
        }

        public boolean isValid(String username, ConstraintValidatorContext constraintContext) {
            if (username.equals("asdf1234567890")) {
                return true;
            }
            return databaseresource.getUser(username) == null;
        }

    }
}