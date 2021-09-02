package com.homeprojects.di.core;

public enum BeanType {

	COMPONENT("component"), ATBEAN("atbean");
	
	private final String value;
	
	private BeanType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
