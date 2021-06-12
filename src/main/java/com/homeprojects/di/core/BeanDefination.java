package com.homeprojects.di.core;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class BeanDefination {
	
	private final String name;
	
	private final String scope;

	private final TypeElement element;
	
	private final List<BeanDefination> dependencies;

	private final ExecutableElement constructor; 
	
	public BeanDefination(String name, String scope, TypeElement element, ExecutableElement constuctor, List<BeanDefination> dependencies) {
		this.name = name;
		this.scope = scope;
		this.element = element;
		this.constructor = constuctor;
		this.dependencies = dependencies;
	}
	
	public String getName() {
		return name;
	}
	
	public String getScope() {
		return scope;
	}
	
	public List<BeanDefination> getDependencies() {
		return dependencies;
	}
	
	public ExecutableElement getConstructor() {
		return constructor;
	}
	
	public TypeElement getElement() {
		return element;
	}
}
