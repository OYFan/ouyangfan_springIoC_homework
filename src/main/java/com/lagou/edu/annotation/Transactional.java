package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * 表明此类需要加入事务控制
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

}
