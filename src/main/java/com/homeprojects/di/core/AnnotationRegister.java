package com.homeprojects.di.core;

import java.lang.annotation.Annotation;

public interface AnnotationRegister {

	Class<? extends Annotation> getAnnotation();
}
