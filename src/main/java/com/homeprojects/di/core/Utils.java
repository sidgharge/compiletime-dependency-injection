package com.homeprojects.di.core;

public class Utils {

	public static boolean isSingleton(BeanDefinition bean) {
		return bean.getScope().equals("singleton");
	}
}
