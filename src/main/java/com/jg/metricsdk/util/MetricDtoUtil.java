package com.jg.metricsdk.util;

public class MetricDtoUtil {

	private static final String keyFormat = "%s.metric.%s";

	public static String getFullKey(String metricHostName, String metricKey) {
		return String.format(keyFormat, metricHostName, metricKey);
	}
}
