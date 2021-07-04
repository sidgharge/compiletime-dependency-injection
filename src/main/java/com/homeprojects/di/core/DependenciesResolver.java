package com.homeprojects.di.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.homeprojects.di.validation.ValidationException;

public class DependenciesResolver {
	
	private final List<BeanToken> tokens;
	
	private final ProcessingEnvironment env;

	private final Map<BeanToken, BeanDefinition> map;
	
	private final Queue<BeanDefinition> queue;
	
	private final Set<String> resolvingQueue = new LinkedHashSet<>();

	public DependenciesResolver(List<BeanToken> tokens, ProcessingEnvironment processingEnv) {
		this.tokens = tokens;
		this.env = processingEnv;
		map = new HashMap<>();
		queue = new LinkedList<>();
	}

	public Queue<BeanDefinition> resolve() {
		for (BeanToken token : tokens) {
			resolve(token);
		}
		return queue;
	}

	private BeanDefinition resolve(BeanToken token) {
		validateForCircularDependency(token);
		if(map.containsKey(token)) {
			return map.get(token);
		}
		resolvingQueue.add(token.getBeanName());
		ExecutableElement initializer = token.getInitializer();
		
		List<BeanDefinition> dependencies = initializer.getParameters()
			.stream()
			.map(ve -> resolveParameter(ve))
			.collect(Collectors.toList());
		
		List<Setter> setters = new ArrayList<>();
		for(ExecutableElement method: token.getSetters()) {
			List<BeanDefinition> setterDeps = method.getParameters().stream()
				.map(param -> resolveParameter(param))
				.collect(Collectors.toList());
			Setter setter = new Setter(method.getSimpleName().toString(), setterDeps, method);
			setters.add(setter);
		}
		
		BeanDefinition definition = new BeanDefinition(token, initializer, dependencies, setters);
		
		map.put(token, definition);
		queue.add(definition);
		resolvingQueue.remove(token.getBeanName());

		token.getAtBeans()
			.stream()
			.map(this::resolve)
			.forEach(bd -> bd.setParentConfig(definition));
		
		return definition;
	}

	private void validateForCircularDependency(BeanToken token) {
		if(!resolvingQueue.contains(token.getBeanName())) {
			return;
		}
		String direction = resolvingQueue.stream()
			// .map(e -> e.getSimpleName())
			.collect(Collectors.joining(" -> "));
		
		throw new ValidationException("Circular dependency found: " + direction);
	}
	
	private BeanDefinition resolveParameter(VariableElement variableElement) {
		TypeMirror type = variableElement.asType();
		TypeElement dependecyElement = (TypeElement) env.getTypeUtils().asElement(type);
		BeanDefinition impl = findImplementation(dependecyElement, variableElement);
		if(impl == null) {
			throw new ValidationException("No bean found", variableElement);
		}
		return impl;
	}
	
	private BeanDefinition findImplementation(TypeElement element, VariableElement variable) {
		List<BeanToken> implementations = findImplementations(element, tokens);
		BeanToken implementation = findExactImplementation(implementations, variable);
		if(implementation != null) {
			return resolve(implementation);
		}
		
		List<BeanToken> implementations2 = tokens.stream()
			.flatMap(token -> token.getAtBeans().stream())
			.filter(atBean -> env.getTypeUtils().isAssignable(atBean.getElement().asType(), element.asType()))
			.collect(Collectors.toList());
		implementation = findExactImplementation(implementations2, variable);
		if(implementation != null) {
			return resolve(implementation);
		}
		
		implementations = new ArrayList<>(implementations);
		implementations.addAll(implementations2);
		
		if(implementations.isEmpty()) {
			throw new ValidationException("No bean found", variable);
		}
		if(implementations.size() > 1) {
			throw new ValidationException("Multiple beans found", variable);
		}
		
		return resolve(implementations.get(0));
	}

	private List<BeanToken> findImplementations(TypeElement element, List<BeanToken> tokens) {
		return tokens.stream()
			.filter(token -> env.getTypeUtils().isAssignable(token.getElement().asType(), element.asType()))
			.filter(token -> !token.getElement().getModifiers().contains(Modifier.ABSTRACT))
			.filter(token -> !token.getElement().getKind().isInterface())
			.collect(Collectors.toList());
	}
	
	private BeanToken findExactImplementation(List<BeanToken> implementations, VariableElement variable) {
//		if(implementations.size() == 1) {
//			return resolve(implementations.get(0));
//		} else if(implementations.size() > 1) {
//			for (BeanToken token : implementations) {
//				if(token.getBeanName().equals(variable.getSimpleName().toString())) {
//					return resolve(token);
//				}
//			}
//		}
//		return null;
		return implementations.stream()
				.filter(token -> token.getBeanName().equals(variable.getSimpleName().toString()))
				.findFirst()
				.orElse(null);
	}

}
