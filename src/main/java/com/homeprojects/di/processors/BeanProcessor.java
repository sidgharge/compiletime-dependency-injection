package com.homeprojects.di.processors;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;
import com.homeprojects.di.annotations.Component;
import com.homeprojects.di.core.BeanDefination;
import com.homeprojects.di.core.DependenciesResolver;
import com.homeprojects.di.core.Generator;

@SupportedAnnotationTypes("com.homeprojects.di.annotations.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BeanProcessor extends AbstractProcessor {
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		List<TypeElement> dependencies = roundEnvironment
			.getElementsAnnotatedWith(Component.class)
			.stream()
			.map(element -> (TypeElement) element)
			.collect(Collectors.toList());
		
		DependenciesResolver resolver = new DependenciesResolver(dependencies, processingEnv);
		Queue<BeanDefination> beans = resolver.resolve();
		if(resolver.hasError()) {
			return false;
		}
		
		new Generator(beans, processingEnv).generate();
		return false;
	}
	
	public void print(String text) {
		processingEnv.getMessager().printMessage(Kind.WARNING, text);
	}

}
