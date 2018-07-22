package com.jg.metricsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpDto {
	private String path;
	private Long value;
	private String timestamp;

	public HttpDto(String fullKey, Long value, Long timestamp) {
		this.path = fullKey;
		this.value = value;
		this.timestamp = String.valueOf(timestamp / 1000);
	}

	public HttpDto(String fullKey, Integer value, Long timestamp) {
		this.path = fullKey;
		this.value = value.longValue();
		this.timestamp = String.valueOf(timestamp / 1000);
	}

	@Override
	public String toString() {
		return String.format("path: %s, value: %s, timestamp: %s", path, value, timestamp);
	}
}
