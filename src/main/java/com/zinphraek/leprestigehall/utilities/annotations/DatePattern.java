package com.zinphraek.leprestigehall.utilities.annotations;

import com.zinphraek.leprestigehall.utilities.validator.DatePatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DatePatternValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatePattern {

  String message() default "Invalid date format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String pattern();
}
