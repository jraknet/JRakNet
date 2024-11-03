package com.whirvis.jraknet;

import java.lang.annotation.*;

/**
 * Indicates the original type used in the C/C++ implementation of RakNet.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
public @interface NativeType {
    String value();
}
