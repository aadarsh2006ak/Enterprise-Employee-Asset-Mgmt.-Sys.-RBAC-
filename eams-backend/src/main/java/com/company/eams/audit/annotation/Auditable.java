package com.company.eams.audit.annotation;

import com.company.eams.entity.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Action type (CREATE, UPDATE, DELETE, ASSIGN, RETURN, STATUS_CHANGE, LOGIN, LOGOUT, EXPORT).
     */
    AuditAction action();

    /**
     * Target domain entity name (e.g. "Department", "Employee", "Asset", "User").
     */
    String entityName();

    /**
     * Optional description of the audited operation.
     */
    String description() default "";
}
