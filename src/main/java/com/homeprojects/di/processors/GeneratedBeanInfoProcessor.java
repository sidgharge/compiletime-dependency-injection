package com.homeprojects.di.processors;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
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
import com.homeprojects.di.core.beaninfo.BeanInfo;

@SupportedAnnotationTypes("com.homeprojects.di.annotations.GeneratedBeanInfo")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GeneratedBeanInfoProcessor extends AbstractProcessor {
	
	private static final String LOCATION = "META-INF/services/" + BeanInfo.class.getName();
	
	private FileObject resource;
	
	private BufferedOutputStream os;
	
	private PrintWriter writer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		try {
			this.resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", LOCATION);
//			this.os = new BufferedOutputStream(resource.openOutputStream());
			writer = new PrintWriter(resource.openWriter());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		List<TypeElement> infos = roundEnvironment.getElementsAnnotatedWith(GeneratedBeanInfo.class)
			.stream()
			.map(e -> (TypeElement)e)
			.collect(Collectors.toList());
		
		if(infos.isEmpty()) {
			return false;
		}
		
		for (TypeElement element : infos) {
			writer.println(element.getQualifiedName().toString());
			writer.flush();
		}
		return false;
	}
	
	public void print(String text) {
		processingEnv.getMessager().printMessage(Kind.ERROR, text);
	}

}
