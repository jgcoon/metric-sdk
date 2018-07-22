package com.jg.metricsdk;

import com.jg.metricsdk.model.MetricType;
import com.jg.metricsdk.model.MetricUnit;
import com.jg.metricsdk.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Slf4j
class MetricUpdaterImpl implements MetricUpdater {

	private MetricService metricService;

	public MetricUpdaterImpl(MetricService metricService) {
		this.metricService = metricService;
	}

	public void countByOne(String key) {
		count(key, 1L);
	}

	public void count(String key, Integer count) {
		count(key, count.longValue());
	}

	public void count(String key, Long count) {
		if(countMetricValueCondition.test(count)) {
			sendMetric( new MetricUnit(MetricType.COUNT, key, count) );
		}
	}

	public void latency(String key, Long startTime) {
		latency(key, startTime, System.currentTimeMillis());
	}

	public void latency(String key, Long startTime, Long endTime) {
		if(latencyMetricValueCondition.test(startTime, endTime)) {
			sendMetric( new MetricUnit(MetricType.LATENCY, key, endTime - startTime) );
		}
	}

	public void sendMetric(MetricUnit metric) {
		try {
			if(metricDtoCondition.test(metric) && keyCondition.test(metric.getKey())) {
				metricService.accept(metric);
			}
		}
		catch(Exception e) {
			log.error("Metric SDK: metric update failed" + e.getMessage());
		}
	}

	private Predicate<String> keyCondition = key -> {
		if(StringUtils.isEmpty(key)) {
			log.error("Metric SDK: Please input right metric key; value = [" + key + "]");
			return false;
		}

		if(!StringUtils.equals(key.trim(), key)) {
			log.warn("Metric SDK: metric key is needed to be trimmed; key = [" + key + "]");
			return false;
		}

		if(key.charAt(key.length() - 1) == '.') {
			log.warn("Metric SDK: end of metric key should not be a comma; key = [" + key + "]");
			return false;
		}
		return true;
	};

	private Predicate<MetricUnit> metricDtoCondition = metricDto -> {
		if(Objects.isNull(metricDto) || CollectionUtils.isEmpty(metricDto.getValue()) ||  Objects.isNull(metricDto.getType()) ) {
			log.error("Metric SDK: invalid metric dto is set");
			return false;
		}
		return true;
	};

	private Predicate<Long> countMetricValueCondition = value -> {
		if(Objects.isNull(value)) {
			log.warn("Metric SDK: null value for count metric is set");
			return false;
		}
		return true;
	};

	private BiPredicate<Long, Long> latencyMetricValueCondition = (initTime, endTime) -> {
		if( Objects.isNull(initTime) || Objects.isNull(endTime) || endTime < initTime || endTime <= 0L || initTime <= 0L ) {
			log.warn("Metric SDK: invalid value fro latency metric is set; initTime = " + initTime + ", endTime = " + endTime);
			return false;
		}
		return true;
	};
}
