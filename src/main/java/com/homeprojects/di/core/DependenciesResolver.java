package com.homeprojects.di.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		resolvingQueue.add(token.getBeanName());
		if(map.containsKey(token)) {
			return map.get(token);
		}
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
		if(!resolvingQueue.contains(token.getElement())) {
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
		BeanDefinition impl = findImplementation(dependecyElement);
		if(impl == null) {
			throw new ValidationException("No implementation found", variableElement);
		}
		return impl;
	}
	
	private BeanDefinition findImplementation(TypeElement element) {
		Optional<BeanToken> optional = tokens.stream()
			.filter(token -> env.getTypeUtils().isAssignable(token.getElement().asType(), element.asType()))
			.filter(token -> !token.getElement().getModifiers().contains(Modifier.ABSTRACT))
			.filter(token -> !token.getElement().getKind().isInterface())
			.findFirst();
		
		if(optional.isPresent()) {
			return resolve(optional.get());
		}
		
		for (BeanToken token : tokens) {
			for (BeanToken atBean : token.getAtBeans()) {
				if(env.getTypeUtils().isAssignable(atBean.getElement().asType(), element.asType())) {
					resolve(token);
					return resolve(atBean);
				}
			}
		}
		return null;
	}

}
