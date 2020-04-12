package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * 依赖按类型注入
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    boolean required() default true;

}
