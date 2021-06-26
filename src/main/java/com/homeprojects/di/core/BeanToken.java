package com.homeprojects.di.core;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class BeanToken {

	private final TypeElement element;
	
	private String type;

	private ExecutableElement initializer;
	
	private List<ExecutableElement> postConstructs;
	
	private List<ExecutableElement> preDestroys;

	private final List<BeanToken> atBeans;

	private String beanName;

	private String scope;
	
	private BeanToken parentConfiguration;
	
	private TypeMirror exactType;

	public BeanToken(TypeElement element) {
		this.element = element;
		this.atBeans = new ArrayList<>();
	}
	
	public void addAtBean(BeanToken token) {
		this.atBeans .add(token);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ExecutableElement getInitializer() {
		return initializer;
	}

	public void setInitializer(ExecutableElement initializer) {
		this.initializer = initializer;
	}

	public List<ExecutableElement> getPostConstructs() {
		return postConstructs;
	}

	public void setPostConstructs(List<ExecutableElement> postConstructs) {
		this.postConstructs = postConstructs;
	}

	public List<ExecutableElement> getPreDestroys() {
		return preDestroys;
	}

	public void setPreDestroys(List<ExecutableElement> preDestroys) {
		this.preDestroys = preDestroys;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public TypeElement getElement() {
		return element;
	}

	public List<BeanToken> getAtBeans() {
		return atBeans;
	}

	public void setParentConfiguration(BeanToken parentConfiguration) {
		this.parentConfiguration = parentConfiguration;
	}
	
	public BeanToken getParentConfiguration() {
		return parentConfiguration;
	}
	
	public void setExactType(TypeMirror exactType) {
		this.exactType = exactType;
	}
	
	public TypeMirror getExactType() {
		return exactType;
	}
}
