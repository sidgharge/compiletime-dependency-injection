package com.homeprojects.di.core;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class BeanDefinition {
	
	private final String name;
	
	private final String scope;

	private final TypeElement element;
	
	private final List<BeanDefinition> dependencies;

	private final ExecutableElement constructor;
	
	private final List<String> postconstrutMethods;

	private final List<String> preDestroyMethods;
	
	public BeanDefinition(String name, String scope, TypeElement element, ExecutableElement constuctor, List<BeanDefinition> dependencies, List<String> postconstrutMethods, List<String> preDestroyMethods) {
		this.name = name;
		this.scope = scope;
		this.element = element;
		this.constructor = constuctor;
		this.dependencies = dependencies;
		this.postconstrutMethods = postconstrutMethods;
		this.preDestroyMethods = preDestroyMethods;
	}
	
	public String getName() {
		return name;
	}
	
	public String getScope() {
		return scope;
	}
	
	public List<BeanDefinition> getDependencies() {
		return dependencies;
	}
	
	public ExecutableElement getConstructor() {
		return constructor;
	}
	
	public TypeElement getElement() {
		return element;
	}
	
	public List<String> getPostconstrutMethods() {
		return postconstrutMethods;
	}
	
	public List<String> getPreDestroyMethods() {
		return preDestroyMethods;
	}
}
