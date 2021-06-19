package com.homeprojects.di.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

public class DependenciesResolver2 {
	
	private final List<BeanToken> tokens;
	
	private final ProcessingEnvironment env;

	private boolean hasErrors = false;
	
	private final Map<BeanToken, BeanDefinition> map;
	
	private final Queue<BeanDefinition> queue;

	public DependenciesResolver2(List<BeanToken> tokens, ProcessingEnvironment processingEnv) {
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
		if(map.containsKey(token)) {
			return map.get(token);
		}
		ExecutableElement initializer = token.getInitializer();
		List<BeanDefinition> dependencies = initializer.getParameters()
		.stream()
		.map(ve -> resolveParameter(ve))
		.collect(Collectors.toList());
		
		BeanDefinition definition = new BeanDefinition(
				token.getBeanName(),
				token.getScope(),
				token.getElement(),
				initializer,
				dependencies,
				token.getPostConstructs().stream().map(m -> m.getSimpleName().toString()).collect(Collectors.toList()),
				token.getPreDestroys().stream().map(m -> m.getSimpleName().toString()).collect(Collectors.toList())
		);
		
		map.put(token, definition);
		queue.add(definition);
		
		token.getAtBeans()
			.stream()
			.map(this::resolve)
			.forEach(bd -> bd.setParentConfig(definition));
		
		return definition;
	}
	
	private BeanDefinition resolveParameter(VariableElement variableElement) {
		TypeMirror type = variableElement.asType();
		TypeElement dependecyElement = (TypeElement) env.getTypeUtils().asElement(type);
		return findImplementation(dependecyElement);
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
		return null; // TODO Handle
	}

}
