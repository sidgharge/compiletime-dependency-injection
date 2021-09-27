package com.homeprojects.di.processors;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import com.google.auto.service.AutoService;
import com.homeprojects.di.annotations.GeneratedBeanInfo;
import com.homeprojects.di.core.beaninfo.BeanInfo;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GeneratedBeanInfoProcessor extends ImplementationFileProccessor {

	public GeneratedBeanInfoProcessor() {
		intefaceClazz = BeanInfo.class;
		annotation = GeneratedBeanInfo.class;
	}
}
