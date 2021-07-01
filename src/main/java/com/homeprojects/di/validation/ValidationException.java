package com.homeprojects.di.validation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private Element element;

	public ValidationException(String message) {
		super(message);
	}
	
	public ValidationException(String message, Element element) {
		super(message);
		this.element = element;
	}
	
	public ValidationException(String message, Element element, AnnotationMirror mirror) {
		super(message);
		this.element = element;
	}
	
	public Element getElement() {
		return element;
	}
}
