package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * 表明此类需要加入IoC容器 Repository标识
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {

    String value() default "";

}