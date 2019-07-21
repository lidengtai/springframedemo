package com.taylor.annotation;

import java.lang.annotation.*;

/**
 * @author taylor
 * @version V1.0
 * @Title: MyRequestMapping
 * @Package: com.taylor.annotation
 * @Description: TODO
 * @date 2019/7/18 0018 22:03
 **/
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}
