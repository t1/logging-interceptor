package com.github.t1.log;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 * The types that a converter can convert. Duplicate converters for the same type will be logged as an error and an
 * arbitrary converter will be picked.
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ConverterType {
    Class<?>[] value();
}
