package com.almaakcorp.entreprise.audit;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Audited {
    String action();
    String entity() default ""; // logical entity name, e.g., "Quotation"
    String entityIdSpEL() default ""; // optional SpEL to resolve id from args/return
}
