package com.jg.metricsdk.annotation;

import com.jg.metricsdk.MetricUpdater;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class MetricAspect {

	private MetricUpdater metricUpdater;

	public MetricAspect(MetricUpdater metricUpdater) {
		this.metricUpdater = metricUpdater;
	}

	@Pointcut("execution(* *(..)) && @annotation(com.jg.metricsdk.annotation.LatencyMetric)")
	public void latencyMetricPointCut() { }

	@Around("latencyMetricPointCut()")
	public Object latencyMetricAround(ProceedingJoinPoint joinPoint) throws Throwable{
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		String key = method.getName();

		com.jg.metricsdk.annotation.LatencyMetric latencyMetric = method.getAnnotation(LatencyMetric.class);

		if (!latencyMetric.key().isEmpty()) {
			key = latencyMetric.key();
		}

		long initTime = System.currentTimeMillis();

		Object retObj = joinPoint.proceed();

		metricUpdater.latency(key, initTime);

		return retObj;
	}
}
