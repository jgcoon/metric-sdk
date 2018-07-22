package com.jg.metricsdk.service;

import com.jg.metricsdk.model.HttpDto;
import com.jg.metricsdk.model.MetricUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LatencyMetricHandler{

	private List<Integer> percentileList;
	private List<Integer> rangeList;

	public LatencyMetricHandler (List<Integer> percentileList, List<Integer> rangeList) {
		this.percentileList = percentileList;
		this.rangeList = rangeList;
	}

	public List<HttpDto> convert(String fullKey, MetricUnit metricUnit) {
		Long timestamp = System.currentTimeMillis();

		//percentile calculation
		List<HttpDto> result = Lists.newArrayList();
		result.addAll(makeLatencyPercentage(fullKey, metricUnit.getValue(), timestamp));

		//range calculation
		Map<String, Long> counterMap = new HashMap<>();
		metricUnit.getValue().forEach(value -> {
			String rangeKey = String.format("%s.range.%s", fullKey, getLatencyRange(value));
			Long prev = counterMap.getOrDefault(rangeKey, 0L);
			counterMap.put(rangeKey, prev + 1);
		});
		result.addAll(counterMap.entrySet().stream()
			.map(entry -> new HttpDto(entry.getKey(), entry.getValue(), timestamp))
			.collect(Collectors.toList()));

		return result;
	}

	private String getLatencyRange(Long value) {
		Long prev = 0L;
		for (Integer range : rangeList) {
			if (value <= range) {
				return prev.toString() + "-" + range.toString();
			}
			prev = range + 1L;
		}
		return prev + "-";
	}

	private List<HttpDto> makeLatencyPercentage(String fullKey, List<Long> values, Long timeStamp) {
		List<HttpDto> list = new LinkedList<>();

		int count = values.size();
		int lastIndex = count - 1;

		for(Integer pecentile : percentileList) {
			Long val = values.get(lastIndex * pecentile / 100);
			list.add(new HttpDto(fullKey + ".p" + pecentile, val, timeStamp));
		}

		Long sum = values.stream().reduce(Long::sum).orElse(0L);
		Long mean = sum / count;
		list.add(new HttpDto(fullKey + ".mean", mean, timeStamp));
		list.add(new HttpDto(fullKey + ".count", count, timeStamp));

		return list;
	}
}
