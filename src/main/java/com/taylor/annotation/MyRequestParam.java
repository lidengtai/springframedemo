package com.taylor.annotation;

import java.lang.annotation.*;

/**
 * @author taylor
 * @version V1.0
 * @Title: MyRequestParam
 * @Package: com.taylor.annotation
 * @Description: TODO
 * @date 2019/7/18 0018 22:06
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value() default "";
}
