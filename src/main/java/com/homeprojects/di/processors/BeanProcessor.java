package com.homeprojects.di.processors;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;
import com.homeprojects.di.core.*;
import com.homeprojects.di.generators.Generator2;

@SupportedAnnotationTypes("com.homeprojects.di.annotations.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BeanProcessor extends AbstractProcessor {
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		List<BeanToken> tokens = new DependeciesFinder(roundEnvironment, processingEnv).find();
		
		DependenciesResolver2 resolver = new DependenciesResolver2(tokens, processingEnv);
		Queue<BeanDefinition> beans = resolver.resolve();
		if(beans.isEmpty() || tokens.isEmpty()) {
			return false;
		}
		
		new Generator2(beans, processingEnv).generate();
		
		return false;
	}
	
	public void print(String text) {
		processingEnv.getMessager().printMessage(Kind.WARNING, text);
	}

}
