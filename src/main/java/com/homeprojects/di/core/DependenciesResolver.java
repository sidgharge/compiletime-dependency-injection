package com.homeprojects.di.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.homeprojects.di.annotations.Component;

public class DependenciesResolver {

	private final List<TypeElement> elements;
	
	private final Map<TypeElement, BeanDefination> map;

	private final ProcessingEnvironment env;
	
	private boolean hasErrors = false;

	public DependenciesResolver(List<TypeElement> dependencies, ProcessingEnvironment processingEnv) {
		this.elements = dependencies;
		this.env = processingEnv;
		this.map = new HashMap<>();
	}

	public Queue<BeanDefination> resolve() {
		Queue<BeanDefination> beans = new PriorityQueue<>((b1, b2) -> b1.getDependencies().size() - b2.getDependencies().size());
		for(TypeElement element: elements) {
			resolve(element).ifPresent(bd -> beans.add(bd));
		}
		return beans;
	}
	
	private Optional<BeanDefination> resolve(TypeElement element) {
		if(map.containsKey(element)) {
			return Optional.ofNullable(map.get(element));
		}
		ExecutableElement constructor = element.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
			.findFirst()
			.map(e -> (ExecutableElement) e)
			.orElseGet(this::error);
		
		List<BeanDefination> dependencies = constructor.getParameters().stream()
			.map(this::resolveParameter)
			.filter(bd -> bd.isPresent())
			.map(bd -> bd.get())
			.collect(Collectors.toList());
		
		if(dependencies.size() != constructor.getParameters().size()) {
			return Optional.empty();
		}
		String name = getBeanName(element);
		String scope = element.getAnnotation(Component.class).scope();
		BeanDefination beanDefination = new BeanDefination(name, scope, element, constructor, dependencies);
		map.put(element, beanDefination);
		return Optional.of(beanDefination);
	}

	private String getBeanName(TypeElement element) {
		Component component = element.getAnnotation(Component.class);
		String name = component.name();
		if(name.isEmpty()) {
			name = element.getSimpleName().toString();
			name = name.length() == 1 ? name.toLowerCase() : Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}

	private Optional<BeanDefination> resolveParameter(VariableElement variableElement) {
		TypeMirror type = variableElement.asType();
		TypeElement dependecyElement = (TypeElement) env.getTypeUtils().asElement(type);
		if(!elements.contains(dependecyElement)) {
			error();
			env.getMessager().printMessage(Kind.ERROR, "Parameter is not a bean", variableElement);
			return Optional.empty();
		}
		return resolve(dependecyElement);
	}
	
	private <T> T error() {
		this.hasErrors = true;
		return null;
	}
	
	public boolean hasError() {
		return hasErrors;
	}

}
