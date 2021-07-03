package com.homeprojects.di.validation;

import java.util.function.Consumer;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private Element element;

	private AnnotationMirror mirror;
	
	private Consumer<Messager> logger;

	public ValidationException(String message) {
		super(message);
		logger = messager -> messager.printMessage(Kind.ERROR, message);
	}
	
	public ValidationException(String message, Element element) {
		super(message);
		this.element = element;
		logger = messager -> messager.printMessage(Kind.ERROR, message, element);
	}
	
	public ValidationException(String message, Element element, AnnotationMirror mirror) {
		super(message);
		this.element = element;
		this.mirror = mirror;
		logger = messager -> messager.printMessage(Kind.ERROR, message, element, mirror);
	}
	
	public Element getElement() {
		return element;
	}
	
	public AnnotationMirror getMirror() {
		return mirror;
	}
	
	public void log(Messager messager) {
		logger.accept(messager);
	}
}
