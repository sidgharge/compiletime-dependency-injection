package com.homeprojects.di.core;

import java.lang.annotation.Annotation;

import com.google.auto.service.AutoService;
import com.homeprojects.di.annotations.Configuration;

@AutoService(AnnotationRegister.class)
public class ConfigAnnotationRegister implements AnnotationRegister {

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Configuration.class;
	}

}
