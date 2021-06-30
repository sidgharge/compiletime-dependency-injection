package com.homeprojects.di.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.homeprojects.di.annotations.Autowired;
import com.homeprojects.di.annotations.Bean;
import com.homeprojects.di.annotations.Component;
import com.homeprojects.di.annotations.Configuration;
import com.homeprojects.di.validation.ValidationException;

public class DependeciesFinder {
	
	private final RoundEnvironment roundEnvironment;
	
	private final ProcessingEnvironment processingEnv;
	
	private final List<BeanToken> tokens;
	
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
			validateAbstractType(element, type);
			token.setInitializer(getConstructor(element));
			token.setBeanName(getBeanName(element));
			token.setScope(getScope(element));
			token.setExactType(element.asType());
			tokens.add(token);
		}
		
		token.setSetters(getMethodsAnnotatedWith(element, Autowired.class));
		token.setPostConstructs(getMethodsAnnotatedWith(element, PostConstruct.class));
		token.setPreDestroys(getMethodsAnnotatedWith(element, PreDestroy.class));
		
		return token;
	}

	private void validateAbstractType(TypeElement element, String type) {
		if(!element.getKind().equals(ElementKind.CLASS) || element.getModifiers().contains(Modifier.ABSTRACT)) {
			String annotation = type.equals("component") ? "@Component" : "@Configuration";
			throw new ValidationException(annotation + " can only be used on concrete classed", element);
		}
	}

	private ExecutableElement getConstructor(TypeElement element) {
		List<ExecutableElement> constructors = element.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
			.map(e -> (ExecutableElement) e)
			.collect(Collectors.toList());
		
		if(constructors.size() == 1) {
			return constructors.get(0);
		}
		List<ExecutableElement> autowiredConstructors = constructors.stream()
				.filter(c -> c.getAnnotation(Autowired.class) != null)
				.collect(Collectors.toList());
		if(autowiredConstructors.size() == 1) {
			return autowiredConstructors.get(0);
		}
		
		if(autowiredConstructors.isEmpty()) {
			throw new ValidationException("At least one constructor needs to be autowired", element);
		}
		
		throw new ValidationException("Only one constructor can be autowired", element);
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
		token.setExactType(beanMethod.getReturnType());
		
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
}
