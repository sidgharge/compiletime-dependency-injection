package com.homeprojects.di.processors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;
import com.homeprojects.di.core.BeanDefinition;
import com.homeprojects.di.core.BeanToken;
import com.homeprojects.di.core.DependeciesFinder;
import com.homeprojects.di.core.DependenciesResolver;
import com.homeprojects.di.generators.Generator;
import com.homeprojects.di.validation.ValidationException;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BeanProcessor extends AbstractProcessor {
	
	public static Set<String> SUPPORTED_ANNOTATIONS = new HashSet<>();
	
	static {
		SUPPORTED_ANNOTATIONS.add("com.homeprojects.di.annotations.Component");
		SUPPORTED_ANNOTATIONS.add("com.homeprojects.di.annotations.Configuration");
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		try {
			List<BeanToken> tokens = new DependeciesFinder(roundEnvironment, processingEnv).find();
			
			DependenciesResolver resolver = new DependenciesResolver(tokens, processingEnv);
			List<BeanDefinition> beans = resolver.resolve();
			if(beans.isEmpty() || tokens.isEmpty()) {
				return false;
			}
			
			new Generator(beans, processingEnv).generate();
		} catch (ValidationException e) {
			e.log(processingEnv.getMessager());
		}
		
		return false;
	}
	
	public void print(String text) {
		processingEnv.getMessager().printMessage(Kind.WARNING, text);
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return SUPPORTED_ANNOTATIONS;
	}

}
