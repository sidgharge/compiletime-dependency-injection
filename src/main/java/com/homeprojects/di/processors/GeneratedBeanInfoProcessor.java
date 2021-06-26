package com.homeprojects.di.processors;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.homeprojects.di.annotations.GeneratedBeanInfo;

@SupportedAnnotationTypes("com.homeprojects.di.annotations.GeneratedBeanInfo")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GeneratedBeanInfoProcessor extends AbstractProcessor {
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		List<TypeElement> infos = roundEnvironment.getElementsAnnotatedWith(GeneratedBeanInfo.class)
			.stream()
			.map(e -> (TypeElement)e)
			.collect(Collectors.toList());
		
		if(infos.isEmpty()) {
			return false;
		}
		
		try {
			FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/com.homeprojects.di.core.beaninfo.BeanInfo");
			
			try(PrintWriter out = new PrintWriter(resource.openWriter())) {
				for (TypeElement element : infos) {
					out.println(element.getQualifiedName());
				}
			}
		} catch (IOException e1) {
			print(e1.getMessage());
		}
		
		return false;
	}
	
	public void print(String text) {
		processingEnv.getMessager().printMessage(Kind.ERROR, text);
	}

}
