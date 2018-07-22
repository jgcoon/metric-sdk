package com.jg.metricsdk.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class KafkaDtoSerializer implements Serializer<com.jg.metricsdk.model.KafkaDto> {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) { }

	@Override
	public byte[] serialize(String topic, KafkaDto data) {
		try {
			return mapper.writeValueAsBytes(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	@Override
	public void close() { }
}
