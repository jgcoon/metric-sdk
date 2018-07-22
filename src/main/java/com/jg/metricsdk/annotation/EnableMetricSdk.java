package com.jg.metricsdk.annotation;

import com.jg.metricsdk.MetricConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ MetricConfig.class })
public @interface EnableMetricSdk {
}
