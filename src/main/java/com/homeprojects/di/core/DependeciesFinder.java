package com.homeprojects.di.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.homeprojects.di.annotations.Bean;
import com.homeprojects.di.annotations.Component;
import com.homeprojects.di.annotations.Configuration;

public class DependeciesFinder {
	
	private final RoundEnvironment roundEnvironment;
	
	private final ProcessingEnvironment processingEnv;
	
	private final List<BeanToken> tokens;
	
	private boolean hasErrors = false;
	
	public DependeciesFinder(RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnv) {
		this.roundEnvironment = roundEnvironment;
		this.processingEnv = processingEnv;
		this.tokens = new ArrayList<>();
	}

	public List<BeanToken> find() {
		roundEnvironment
				.getElementsAnnotatedWith(Component.class)
				.stream()
				.map(element -> (TypeElement) element)
				.forEach(element -> processComponent(element, "component"));
		
		roundEnvironment
				.getElementsAnnotatedWith(Configuration.class)
				.stream()
				.map(element -> (TypeElement) element)
				.forEach(this::processConfiguration);
		
		return tokens;
	}

	private BeanToken processComponent(TypeElement element, String type) {
		BeanToken token = new BeanToken(element);
		token.setType(type);
		if(type.equals("component") || type.equals("configuration")) {
			token.setInitializer(getConstructor(element));
			token.setBeanName(getBeanName(element));
			token.setScope(getScope(element));
			tokens.add(token);
		}
		
		token.setPostConstructs(getMethodsAnnotatedWith(element, PostConstruct.class));
		token.setPreDestroys(getMethodsAnnotatedWith(element, PreDestroy.class));
		
		return token;
	}

	private ExecutableElement getConstructor(TypeElement element) {
		return element.getEnclosedElements().stream()
				.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
				.findFirst()
				.map(e -> (ExecutableElement) e)
				.orElseGet(this::error); // TODO Handle error
	}
	
	private String getBeanName(TypeElement element) {
		String name = "";
		Component component = element.getAnnotation(Component.class);
		if(component != null) {
			name = component.name();
		}
		if(name.isEmpty()) {
			name = element.getSimpleName().toString();
			name = name.length() == 1 ? name.toLowerCase() : Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}
	
	private String getScope(TypeElement element) {
		Component component = element.getAnnotation(Component.class);
		return component == null ? "singleton" : component.scope();
	}
	
	private void processConfiguration(TypeElement element) {
		BeanToken token = processComponent(element, "configuration");
		
		List<ExecutableElement> beanMethods = getMethodsAnnotatedWith(element, Bean.class);
		beanMethods.forEach(bm -> proccessBeanMethod(token, bm));
	}
	
	private void proccessBeanMethod(BeanToken parent, ExecutableElement beanMethod) {
		TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(beanMethod.getReturnType());
		BeanToken token = processComponent(element, "atbean");
		token.setInitializer(beanMethod);
		
		token.setBeanName(getAtBeanBeanName(beanMethod));
		token.setScope(beanMethod.getAnnotation(Bean.class).scope());
		
		parent.addAtBean(token);
		token.setParentConfiguration(parent);
	}
	
	private String getAtBeanBeanName(ExecutableElement beanMethod) {
		String name = beanMethod.getAnnotation(Bean.class).name();
		return name.isBlank() ? beanMethod.getSimpleName().toString() : name;
	}

	private <A extends Annotation> List<ExecutableElement> getMethodsAnnotatedWith(TypeElement element, Class<A> clazz) {
		return element.getEnclosedElements()
			.stream()
			.filter(ee -> ee.getKind().equals(ElementKind.METHOD))
			.filter(method -> method.getAnnotation(clazz) != null)
			.map(method -> (ExecutableElement) method)
			.collect(Collectors.toList());
	}
	
	private <T> T error() {
		this.hasErrors = true;
		return null;
	}
}
