package com.taylor.annotation;

import java.lang.annotation.*;

/**
 * @author taylor
 * @version V1.0
 * @Title: MyService
 * @Package: com.taylor.annotation
 * @Description: TODO
 * @date 2019/7/18 0018 22:04
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
    String value() default "";
}
