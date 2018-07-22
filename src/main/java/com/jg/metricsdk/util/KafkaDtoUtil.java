package com.jg.metricsdk.util;

public class KafkaDtoUtil {

	private static final String keyFormat = "metric.%s";

	public static String getFullKey(String metricHostName, String metricKey) {
		return String.format(keyFormat, metricHostName, metricKey);
	}
}
