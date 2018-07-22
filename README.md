How to use metric SDK - (v.1.0.0)
SDK to measure application or code level metrics for projects based on Spring Framework

(1) Please add following properties on metric to your spring project
* mandatory

		metric.basic.path	: basic metric key
		cf. structure of metric key is as follows: (basic metric path).metric.(metric key)
		metric.tx.mode		: metric data tx mode, (http, kafka)
		metric.http.host	: for http tx mode, http host url
		metric.kafka.host	: for kafka tx mode, kafka host
		metric.kafka.topic	: for kafka tx mode, kafka topic
* optional

		metric.thread.max		: Maximum thread pool size for metric processin (default: 20)
		metric.thread.min		: Minimum thread pool size for metric processing (default: 10)
		metric.thread.alivetime		: Thead alive time for non-running threads (default: 20000 ms)
		metric.window.time		: window time for first-step reducing (default: 10 sec)
		metric.latency.percentile	: precentile values for latency metrics	(default: 50,90,95,99,100)
		metric.latency.range		: categorized range values for for latency metrics	(default: 50,100,150,200,250,300 ms)

(2) Import MetricConfig.class to your project by using '@EnableMetricSdk' annotation
* Add @EnableMetricSdk to main application class (for spring boot) or configuration class (for spring)

(3) API for metric collection

1. Count metric : accumulated value for each key in a minute is calculated
	
		Metric.key( String key ).countByOne().send();
		Metric.key( String key ).count( Integer or Long value ).send();
		Metric.key( String key ).countIfTrue (Boolean condition).send();
    	Metric.key( String key ).countIfFalse (Boolean condition).send();
 
2. Latency metric : millisecond-united latency metrics are calculated
		
		ex.
		Long startTime = System.currentMilliseconds();
    	Metric.key( String key ).latency( startTimee ).send();
    	
		ex.
		Long startTime = System.currentMilliseconds();
    	Long endTime = System.currentMilliseconds();
    	Metric.key( String key ).latency(startTime, endTime).send();

3. For Rxjava, you can measure count or latency metrics by using

		Observable.compose(MetricObs)
		
4. By adding @LatencyMetric in a public method, latency metic for the method can be measured
		
		ex. 
		@LatencyMetric(key = "yourLatencyMetricKey") 
    	public void test(String example) 
     	{ //.. } 


(4) Please check whether library dependency of your project is overlapped with that of Metric SDK
 
	compile 'io.reactivex.rxjava2:rxjava:2.1.7'
	compile "io.reactivex:rxjava:1.3.8"
	compileOnly "org.projectlombok:lombok:1.16.16"
	compileOnly 'org.springframework:spring-context:4.1.6.RELEASE'
	compileOnly 'org.assertj:assertj-core:2.6.0'
	compileOnly 'org.apache.commons:commons-lang3:3.6'
	compileOnly 'org.apache.commons:commons-collections4:4.1'
	compileOnly 'com.google.code.gson:gson:2.8.4'
	compileOnly "org.aspectj:aspectjrt:1.8.10"
	compileOnly group: 'org.apache.kafka', name: 'kafka-clients', version: '0.10.0.0'
	compileOnly group: 'com.fasterxml.jackson.core', name: 'jackson-core', version: '2.9.0'
	compileOnly group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.9.0'
