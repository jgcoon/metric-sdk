package com.jg.metricsdk.model;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Builder
@Data
@Setter
public class KafkaDto {
	final String key;
	final String type;
	String basicPath;

	// metric
	Long timestamp;
	final List<Long> value;
}