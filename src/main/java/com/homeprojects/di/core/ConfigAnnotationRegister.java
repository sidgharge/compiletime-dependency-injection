package com.homeprojects.di.core;

import java.lang.annotation.Annotation;

import com.google.auto.service.AutoService;
import com.homeprojects.di.annotations.Configuration;

@AutoService(AnnonationRegister.class)
public class ConfigAnnotationRegister implements AnnonationRegister {

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Configuration.class;
	}

}
