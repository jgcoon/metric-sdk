package com.jg.metricsdk.util;

public class LongHolder {

	private long value;

	public LongHolder(long value){
		super();
		this.value = value;
	}

	public long get(){
		return value;
	}
	public void set(long value){
		this.value = value;
	}
}