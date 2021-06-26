//package com.homeprojects.di.core;
//
//import java.lang.annotation.Annotation;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Queue;
//import java.util.stream.Collectors;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ElementKind;
//import javax.lang.model.element.ExecutableElement;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.element.VariableElement;
//import javax.lang.model.type.TypeMirror;
//import javax.tools.Diagnostic.Kind;
//
//import com.homeprojects.di.annotations.Bean;
//import com.homeprojects.di.annotations.Component;
//import com.homeprojects.di.annotations.Configuration;
//
//public class DependenciesResolver {
//
//	private final List<TypeElement> components;
//	
//	private final Map<TypeElement, BeanDefinition> map;
//
//	private final ProcessingEnvironment env;
//	
//	private boolean hasErrors = false;
//
//	private final Queue<BeanDefinition> beans;
//	
//	public DependenciesResolver(List<TypeElement> dependencies, ProcessingEnvironment processingEnv) {
//		this.components = dependencies;
//		this.env = processingEnv;
//		this.map = new HashMap<>();
//		beans = new LinkedList<>();
//	}
//
//	public Queue<BeanDefinition> resolve() {
//		for(TypeElement element: components) {
//			resolve(element);
//		}
//		return beans;
//	}
//	
//	private Optional<BeanDefinition> resolve(TypeElement element) {
//		if(map.containsKey(element)) {
//			return Optional.ofNullable(map.get(element));
//		}
//		ExecutableElement constructor = element.getEnclosedElements().stream()
//			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
//			.findFirst()
//			.map(e -> (ExecutableElement) e)
//			.orElseGet(this::error);
//		
//		List<BeanDefinition> dependencies = constructor.getParameters().stream()
//			.map(this::resolveParameter)
//			.filter(bd -> bd.isPresent())
//			.map(bd -> bd.get())
//			.collect(Collectors.toList());
//		
//		if(dependencies.size() != constructor.getParameters().size()) {
//			return Optional.empty();
//		}
//		String name = getBeanName(element);
//		String scope = getScope(element);
//
//		List<String> postconstructMethods = getMethodsAnnotatedWith(element, PostConstruct.class);
//		List<String> preDestroyMethods = getMethodsAnnotatedWith(element, PreDestroy.class);
//			
//		BeanDefinition beanDefination = new BeanDefinition(name, scope, element, constructor, dependencies, postconstructMethods, preDestroyMethods);
//		map.put(element, beanDefination);
//		beans.add(beanDefination);
//		
//		if(element.getAnnotation(Configuration.class) != null) {
//			findBeansConfiguration(beanDefination);
//		}
//		return Optional.of(beanDefination);
//	}
//
//	private void findBeansConfiguration(BeanDefinition beanDefination) {
//		List<String> beanMethods = getMethodsAnnotatedWith(beanDefination.getElement(), Bean.class);
//		beanDefination.addBeanMethods(beanMethods);
//	}
//
//	private String getScope(TypeElement element) {
//		Component component = element.getAnnotation(Component.class);
//		return component == null ? "singleton" : component.scope();
//	}
//
//
//	private <A extends Annotation> List<String> getMethodsAnnotatedWith(TypeElement element, Class<A> clazz) {
//		List<String> postconstructMethods = element.getEnclosedElements()
//			.stream()
//			.filter(ee -> ee.getKind().equals(ElementKind.METHOD))
//			.filter(method -> method.getAnnotation(clazz) != null)
//			.map(method -> method.getSimpleName().toString())
//			.collect(Collectors.toList());
//		return postconstructMethods;
//	}
//
//	private String getBeanName(TypeElement element) {
//		String name = "";
//		Component component = element.getAnnotation(Component.class);
//		if(component != null) {
//			name = component.name();
//		}
//		if(name.isEmpty()) {
//			name = element.getSimpleName().toString();
//			name = name.length() == 1 ? name.toLowerCase() : Character.toLowerCase(name.charAt(0)) + name.substring(1);
//		}
//		return name;
//	}
//
//	private Optional<BeanDefinition> resolveParameter(VariableElement variableElement) {
//		TypeMirror type = variableElement.asType();
//		TypeElement dependecyElement = (TypeElement) env.getTypeUtils().asElement(type);
//		Optional<TypeElement> implementation = findImplementation(dependecyElement);
//		if(implementation.isEmpty()) {
//			error();
//			env.getMessager().printMessage(Kind.ERROR, "Parameter is not a bean", variableElement);
//			return Optional.empty();
//		}
//		return resolve(implementation.get());
//	}
//	
//	private Optional<TypeElement> findImplementation(TypeElement element) {
//		return components.stream()
//			.filter(e -> env.getTypeUtils().isAssignable(e.asType(), element.asType()))
//			.filter(e -> !e.getModifiers().contains(Modifier.ABSTRACT))
//			.filter(e -> !e.getKind().isInterface())
//			.findFirst();
//	}
//	
//	private <T> T error() {
//		this.hasErrors = true;
//		return null;
//	}
//	
//	public boolean hasError() {
//		return hasErrors;
//	}
//
//}
