package com.taylor.annotation;

import java.lang.annotation.*;

/**
 * @author taylor
 * @version V1.0
 * @Title: MyAutowired
 * @Package: com.taylor.annotation
 * @Description: TODO
 * @date 2019/7/18 0018 22:05
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}
