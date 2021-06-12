package com.homeprojects.di.core;

public class ApplicationContext {

	private static ApplicationContext instance = new ApplicationContext();

	public static ApplicationContext getInstance() {
		return instance;
	}
}
