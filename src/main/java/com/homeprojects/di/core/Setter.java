package com.homeprojects.di.core;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public class Setter {

	private final String name;
	
	private final List<BeanDefinition> dependencies;
	
	private final ExecutableElement method;

	public Setter(String name, List<BeanDefinition> dependencies, ExecutableElement method) {
		this.name = name;
		this.dependencies = dependencies;
		this.method = method;
	}
	
	public String getName() {
		return name;
	}
	
	public List<BeanDefinition> getDependencies() {
		return dependencies;
	}
	
	public ExecutableElement getMethod() {
		return method;
	}
}
