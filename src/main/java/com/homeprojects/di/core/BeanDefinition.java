package com.homeprojects.di.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class BeanDefinition {
	
	private final String name;
	
	private final String scope;

	private final TypeElement element;
	
	private final List<BeanDefinition> dependencies;

	private final ExecutableElement constructor;
	
	private final List<Setter> setters;
	
	private final List<String> postconstrutMethods;

	private final List<String> preDestroyMethods;
	
	private BeanDefinition parentConfig;
	
	private final TypeMirror exactType;
	
	public BeanDefinition(BeanToken token, ExecutableElement initializer, List<BeanDefinition> dependencies, List<Setter> setters) {
		this.name = token.getBeanName();
		this.scope = token.getScope();
		this.element = token.getElement();
		this.constructor = initializer;
		this.setters = setters;
		this.dependencies = dependencies;
		this.postconstrutMethods = token.getPostConstructs().stream().map(m -> m.getSimpleName().toString()).collect(Collectors.toList());
		this.preDestroyMethods = token.getPreDestroys().stream().map(m -> m.getSimpleName().toString()).collect(Collectors.toList());
		this.exactType = token.getExactType();
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

	public void addBeanMethods(List<String> beanMethods) {
		this.postconstrutMethods.addAll(beanMethods);
	}
	
	public void setParentConfig(BeanDefinition parentConfig) {
		this.parentConfig = parentConfig;
	}
	
	public BeanDefinition getParentConfig() {
		return parentConfig;
	}
	
	public TypeMirror getExactType() {
		return exactType;
	}
	
	public List<Setter> getSetters() {
		return setters;
	}
}
