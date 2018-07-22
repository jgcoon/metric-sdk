package com.jg.metricsdk.model;

import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.List;

@Data
public class MetricUnit {
	private com.jg.metricsdk.model.MetricType type;
	private String key;
	private List<Long> value;

	public MetricUnit(MetricType type, String key, Long value) {
		this.type = type;
		this.key = key;
		this.value = Lists.newArrayList(value);
	}
}
