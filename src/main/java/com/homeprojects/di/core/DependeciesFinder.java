package com.homeprojects.di.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import com.homeprojects.di.annotations.Autowired;
import com.homeprojects.di.annotations.Bean;
import com.homeprojects.di.annotations.Component;
import com.homeprojects.di.generators.CompositeAnnotationWriter;
import com.homeprojects.di.processors.BeanProcessor;
import com.homeprojects.di.validation.ValidationException;

public class DependeciesFinder {
	
	private static boolean loaded = false;
	
	private final RoundEnvironment roundEnvironment;
	
	private final ProcessingEnvironment processingEnv;
	
	private final List<BeanToken> tokens;
	
	private final Set<TypeElement> compositeAnnotations;
	
	public DependeciesFinder(RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnv) {
		this.roundEnvironment = roundEnvironment;
		this.processingEnv = processingEnv;
		this.tokens = new ArrayList<>();
		compositeAnnotations = new HashSet<>();
	}

	public List<BeanToken> find() throws IOException {
		findComponents();
		findConfigs();
		registerInheritedAnnotations();
		return tokens;
	}

	private void registerInheritedAnnotations() throws IOException {
		new CompositeAnnotationWriter(processingEnv, compositeAnnotations).write();;
	}

	private void findComponents() {
		for(Element element: roundEnvironment.getElementsAnnotatedWith(Component.class)) {
			if(element.getKind().equals(ElementKind.ANNOTATION_TYPE)) {
				processInheritedAnnotation((TypeElement) element, element.getAnnotation(Component.class));
			} else {
				processComponent((TypeElement) element, BeanType.COMPONENT, element.getAnnotation(Component.class));				
			}
		}
	}
	
	private void findConfigs() {
		// Doing this because @Configuration is already compiled
		// and compiler won't find it
		// So we're getting it from classpath
//		TypeElement configElement = processingEnv.getElementUtils().getTypeElement(Configuration.class.getName());
//		processInheritedAnnotation(configElement, configElement.getAnnotation(Component.class));
		if(loaded) {
			return;
		}
		ServiceLoader<AnnotationRegister> serviceLoader = ServiceLoader.load(AnnotationRegister.class, this.getClass().getClassLoader());
		
		for (AnnotationRegister annotationRegister : serviceLoader) {
			System.out.println(annotationRegister.getAnnotation());
			Optional<Annotation> optional = getComponent(annotationRegister.getAnnotation().getAnnotations());
			if(!optional.isPresent()) {
				continue;
			}
			roundEnvironment.getElementsAnnotatedWith(annotationRegister.getAnnotation())
				.forEach(element -> processComponent((TypeElement)element, BeanType.COMPONENT, optional.get()));
		}
		loaded = true;
		
//		for(Element element: roundEnvironment.getElementsAnnotatedWith(Configuration.class)) {
//			if(element.getKind().equals(ElementKind.ANNOTATION_TYPE)) {
//				processInheritedAnnotation((TypeElement) element, element.getAnnotation(Configuration.class));
//			} else {
//				processConfiguration((TypeElement) element, element.getAnnotation(Configuration.class));
//			}
//		}
	}
	
	private Optional<Annotation> getComponent(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if(annotation.annotationType().equals(Component.class)) {
				return Optional.of(annotation);
			}
			Optional<Annotation> optional = getComponent(annotation.getClass().getAnnotations());
			if(optional.isPresent()) {
				return optional;
			}
		}
		return Optional.empty();
	}

	private BeanToken processComponent(TypeElement element, BeanType type, Annotation annotation) {
		BeanToken token = new BeanToken(element);
		token.setType(type.getValue());
		if(type.equals(BeanType.COMPONENT)) {
			validateAbstractType(element);
			token.setInitializer(validateConstructorNotPrivate(getConstructor(element)));
			token.setBeanName(getBeanName(element, annotation));
			token.setScope(getScope(element, annotation));
			token.setExactType(element.asType());
			tokens.add(token);
		}
		
		getMethodsAnnotatedWith(element, Bean.class).forEach(bm -> proccessBeanMethod(token, bm, annotation));
		token.setSetters(getMethodsAnnotatedWith(element, Autowired.class));
		token.setPostConstructs(getMethodsAnnotatedWith(element, PostConstruct.class));
		token.setPreDestroys(getMethodsAnnotatedWith(element, PreDestroy.class));
		
		return token;
	}

	private void processInheritedAnnotation(TypeElement element, Annotation annotation) {
		compositeAnnotations.add(element);
		for(Element e: roundEnvironment.getElementsAnnotatedWith(element)) {
			if(e.getKind().equals(ElementKind.ANNOTATION_TYPE)) {
				processInheritedAnnotation((TypeElement) e, annotation);
			} else {
				processComponent((TypeElement) e, BeanType.COMPONENT, annotation);
			}
		}
	}

	private void validateAbstractType(TypeElement element) {
		if(!element.getKind().equals(ElementKind.CLASS) || element.getModifiers().contains(Modifier.ABSTRACT)) {
			List<? extends AnnotationMirror> mirrors = processingEnv.getElementUtils().getAllAnnotationMirrors(element);
			for (AnnotationMirror mirror : mirrors) {
				DeclaredType declaredType = mirror.getAnnotationType();
				String annotationName = ((TypeElement)declaredType.asElement()).getQualifiedName().toString();
				if(BeanProcessor.SUPPORTED_ANNOTATIONS.contains(annotationName)) {
					throw new ValidationException("This annotation can only be used on concrete classed", element, mirror);
				}
			}
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
	
	private ExecutableElement validateConstructorNotPrivate(ExecutableElement constructor) {
		if(constructor.getModifiers().contains(Modifier.PRIVATE)) {
			throw new ValidationException("Constructor cannot be private", constructor);
		}
		return constructor;
	}
	
	private String getBeanName(TypeElement element, Annotation annotation) {
		String name = null;
		if(annotation instanceof Component) {
			name = ((Component) annotation).name();
		}
		if(name == null || name.isEmpty()) {
			name = element.getSimpleName().toString();
			name = name.length() == 1 ? name.toLowerCase() : Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}
	
	private String getScope(TypeElement element, Annotation annotation) {
		if(annotation instanceof Component) {
			return ((Component) annotation).scope();
		}
		return "singleton";
	}
	
	private void proccessBeanMethod(BeanToken parent, ExecutableElement beanMethod, Annotation annotation) {
		TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(beanMethod.getReturnType());
		BeanToken token = processComponent(element, BeanType.ATBEAN, annotation);
		token.setInitializer(beanMethod);
		
		token.setBeanName(getAtBeanBeanName(beanMethod));
		token.setScope(beanMethod.getAnnotation(Bean.class).scope());
		token.setExactType(beanMethod.getReturnType());
		
		parent.addAtBean(token);
		token.setParentConfiguration(parent);
	}
	
	private String getAtBeanBeanName(ExecutableElement beanMethod) {
		String name = beanMethod.getAnnotation(Bean.class).name();
		return name.trim().isEmpty() ? beanMethod.getSimpleName().toString() : name;
	}

	private <A extends Annotation> List<ExecutableElement> getMethodsAnnotatedWith(TypeElement element, Class<A> clazz) {
		List<ExecutableElement> methods = new ArrayList<>();
		for(Element e: element.getEnclosedElements()) {
			if(!e.getKind().equals(ElementKind.METHOD) || e.getAnnotation(clazz) == null) {
				continue;
			}
			ExecutableElement method = (ExecutableElement) e;
			if(method.getModifiers().contains(Modifier.PRIVATE)) {
				throw new ValidationException("Method with @" + clazz.getSimpleName() + " should not be private", method);
			}
			methods.add(method);
		}
		return methods;
		
	}
	
}
