package com.jg.metricsdk.publisher;


import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.KafkaDto;
import com.jg.metricsdk.model.KafkaDtoSerializer;
import com.jg.metricsdk.model.MetricUnit;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.util.Lists;

import java.util.Properties;

@Slf4j
public class KafkaMetricPublisher implements Consumer<MetricUnit> {

	private String metricKafkaHost;
	private String metricKafkaTopic;

	private String metricBasicPath;

	private KafkaProducer<String, KafkaDto> kafkaProducer;
	private MetricSchedFactory metricSchedFactory;

	public KafkaMetricPublisher(String metricKafkaHost, String metricKafkaTopic, String metricBasicPath, MetricSchedFactory metricSchedFactory) {
		this.metricKafkaHost = metricKafkaHost;
		this.metricKafkaTopic = metricKafkaTopic;
		this.metricBasicPath = metricBasicPath;

		this.kafkaProducer = new KafkaProducer<>(kafkaProducerProperties());
		this.metricSchedFactory = metricSchedFactory;
	}

	private Properties kafkaProducerProperties() {
		Properties properties = new Properties();
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaDtoSerializer.class.getName());
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, metricKafkaHost);
		return properties;
	}

	@Override
	public void accept(MetricUnit metricUnit) {
		Observable.just(metricUnit)
			.map(metric -> KafkaDto.builder()
				.basicPath(metricBasicPath)
				.timestamp(System.currentTimeMillis())
				.key(metric.getKey())
				.value(Lists.newArrayList(metric.getValue()))
				.type(metric.getType().name())
				.build()
			)
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.subscribe(dto ->
				this.kafkaProducer.send(new ProducerRecord<>(metricKafkaTopic, dto.getBasicPath(), dto))
			);
	}
}

