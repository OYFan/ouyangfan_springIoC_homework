package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * 表明此类需要加入IoC容器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";

}
