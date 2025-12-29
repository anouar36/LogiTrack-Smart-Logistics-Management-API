package com.logitrack.logitrack.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce ownership checking on controller methods.
 * Ensures a CLIENT can only access their own resources.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckOwnership {
    /**
     * The name of the parameter containing the resource ID
     */
    String resourceIdParam() default "id";
    
    /**
     * The type of resource being accessed
     */
    ResourceType resourceType();
    
    enum ResourceType {
        SALES_ORDER,
        SHIPMENT,
        CLIENT_PROFILE
    }
}
