package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * 依赖注入时指定beanId
 */
@Target({ElementType.FIELD,  ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
    String value() default "";
}