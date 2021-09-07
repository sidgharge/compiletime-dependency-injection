package com.homeprojects.di.core;

import java.lang.annotation.Annotation;

public interface AnnonationRegister {

	Class<? extends Annotation> getAnnotation();
}
