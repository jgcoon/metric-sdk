package com.jg.metricsdk;

import com.jg.metricsdk.model.MetricType;
import com.jg.metricsdk.model.MetricUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Slf4j
public class MetricBuilder {

	@Getter(AccessLevel.PROTECTED)
	private String key;
	@Getter(AccessLevel.PROTECTED)
	private MetricUnit metricUnit;

	@Getter(AccessLevel.PROTECTED)
	private boolean overwrite;

	@Getter(AccessLevel.PROTECTED)
	private boolean sendZeroValue;

	/**
	 * Metric Key
	 * @param key
	 */
	MetricBuilder (@NonNull String key) {
		this.overwrite = false;
		this.sendZeroValue = false;
		this.key = key.trim();
	}

	/**
	 * Count Metric
	 */
	public MetricBuilder countByOne () {
		return count(1L);
	}

	/**
	 * Count Metric
	 */
	public MetricBuilder countIfTrue (Boolean condition) {
		if(condition) {
			return count(1L);
		}
		return count(0L);
	}

	/**
	 * Count Metric
	 */
	public MetricBuilder countIfFalse (Boolean condition) {
		if(!condition) {
			return count(1L);
		}
		return count(0L);
	}

	public MetricBuilder sendZeroValue (Boolean sendZeroValue) {
		this.sendZeroValue = sendZeroValue;
		return this;
	}

	/**
	 * Count Metric
	 * @param value
	 */
	public MetricBuilder count (Integer value) {
		return count(value.longValue());
	}

	/**
	 * Count Metric
	 * @param value
	 */
	public MetricBuilder count (Long value) {
		this.metricUnit = new MetricUnit(MetricType.COUNT, this.key, value);
		return this;
	}

	/**
	 * Latency Metric
	 * @param startTime
	 * @return MetricBuilder
	 */
	public MetricBuilder latency (Long startTime) {
		this.metricUnit = new MetricUnit(MetricType.LATENCY, this.key, System.currentTimeMillis() - startTime);
		return this;
	}

	/**
	 * Latency Metric
	 * @param startTime
	 * @param endTime
	 * @return MetricBuilder
	 */
	public MetricBuilder latency (Long startTime, Long endTime) {
		this.metricUnit = new MetricUnit(MetricType.LATENCY, this.key, endTime - startTime);
		return this;
	}

	/**
	 * metric/message sending
	 */
	public void send () {
		if( StringUtils.isEmpty(this.getKey()) ) {
			log.warn("Metric SDK: metric key should not be empty");
			return;
		}

		// metric
		if( Objects.isNull(this.getMetricUnit()) ) {
			log.warn("Metric SDK: metric value should not be empty; metric key = " + this.key);
			return;
		}

		// send zero value
		if(this.metricUnit.getValue().get(0) == 0L && !sendZeroValue) {
			return;
		}

		Metric.process(this.getMetricUnit());
	}
}