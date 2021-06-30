package com.homeprojects.di.core;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class Utils {

	public static boolean isSingleton(BeanDefinition bean) {
		return bean.getScope().equals("singleton");
	}
	
	public static String getPackageName(TypeElement typeElement) {
		return ((PackageElement)typeElement.getEnclosingElement()).getQualifiedName().toString();
	}
}
