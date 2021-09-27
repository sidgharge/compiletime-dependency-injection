package com.homeprojects.di.processors;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public abstract class ImplementationFileProccessor extends AbstractProcessor {

	private String loaction = "META-INF/services/";

	protected Class<? extends Annotation> annotation;

	protected Class<?> intefaceClazz;

	private FileObject resource;

	private PrintWriter writer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		try {
			this.loaction = loaction + intefaceClazz.getName();
			this.resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", loaction);
			this.writer = new PrintWriter(resource.openWriter());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		List<TypeElement> infos = roundEnvironment.getElementsAnnotatedWith(annotation).stream()
				.map(e -> (TypeElement) e).collect(Collectors.toList());

		if (infos.isEmpty()) {
			return false;
		}

		for (TypeElement element : infos) {
			writer.println(element.getQualifiedName().toString());
			writer.flush();
		}
		return false;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> anns = new HashSet<>();
		anns.add(annotation.getName());
		return anns;
	}
}
