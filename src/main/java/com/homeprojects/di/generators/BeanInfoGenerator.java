package com.homeprojects.di.generators;

import static com.homeprojects.di.core.Utils.isSingleton;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;

import com.homeprojects.di.annotations.GeneratedBeanInfo;
import com.homeprojects.di.core.BeanDefinition;
import com.homeprojects.di.core.beaninfo.AbstractBeanInfo;
import com.homeprojects.di.core.beaninfo.AbstractSingletonBeanInfo;
import com.homeprojects.di.core.beaninfo.BeanInfoRegister;
import com.homeprojects.di.core.factory.BeanFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

public class BeanInfoGenerator {
	
	private final BeanDefinition def;
	
	private final ProcessingEnvironment env;

	public BeanInfoGenerator(BeanDefinition bean, ProcessingEnvironment env) {
		this.def = bean;
		this.env = env;
	}

	public void processBeanDefinition() {
		String className = getClassName();
		TypeSpec.Builder builder = TypeSpec.
				classBuilder(className)
				.addModifiers(Modifier.PUBLIC)
				.superclass(getSuperClass())
				.addAnnotation(GeneratedBeanInfo.class)
//				.addStaticBlock(getStaticRegistrationBlock(className))
				.addMethod(getConstructor())
				.addMethod(getBuildMethod());
		
		if(!isSingleton(def)) {
			builder.addMethod(getGetInstanceMethod());
			builder.addMethod(getGetTypeMethod());
			builder.addMethod(getGetScopeMethod());
		}

		JavaFile file = JavaFile.builder(getPackage(), builder.build()).build();

		try {
			file.writeTo(env.getFiler());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Override
//	public Class<?> getType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getScope() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	private MethodSpec getGetTypeMethod() {
		return MethodSpec.methodBuilder("getType")
				.addModifiers(Modifier.PUBLIC)
				.returns(getGetTypeReturnType())
				.addAnnotation(Override.class)
				.addStatement("return $T.class", def.getElement())
				.build();
	}

	private ParameterizedTypeName getGetTypeReturnType() {
		return ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
	}
	
	private MethodSpec getGetScopeMethod() {
		return MethodSpec.methodBuilder("getScope")
				.addModifiers(Modifier.PUBLIC)
				.returns(String.class)
				.addAnnotation(Override.class)
				.addStatement("return $S", def.getScope())
				.build();
	}

	private MethodSpec getGetInstanceMethod() {
		return MethodSpec.methodBuilder("getInstance")
				.addModifiers(Modifier.PUBLIC)
				.returns(getTypeNameForElement())
				.addAnnotation(Override.class)
				.addStatement("return build()")
				.build();
	}

	private MethodSpec getBuildMethod() {
		return MethodSpec.methodBuilder("build")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.returns(getTypeNameForElement())
			.addCode(getBuildMethodBody())
			.build();
	}

	private TypeName getTypeNameForElement() {
		return TypeName.get(def.getElement().asType());
	}

	private CodeBlock getBuildMethodBody() {
		Builder builder = CodeBlock.builder();
		
		for (BeanDefinition dependency : def.getDependencies()) {
			builder.addStatement("$T $L = beanFactory.getBean($T.class)", dependency.getElement(), dependency.getName(), dependency.getElement());
		}
		
		String dependencyNamesCommaSeprataed = def.getDependencies().stream()
				.map(dependency -> dependency.getName())
				.collect(Collectors.joining(", "));
		
		if(def.getParentConfig() != null) {
			BeanDefinition parent = def.getParentConfig();
			builder.addStatement("$T $L = beanFactory.getBean($T.class)", parent.getElement(), parent.getName(), parent.getElement());
			builder.addStatement("$T $L = $L.$L($L)", def.getElement(), def.getName(), parent.getName(), def.getConstructor().getSimpleName(), dependencyNamesCommaSeprataed);
		} else {
			builder.addStatement("$T $L = new $T($L)", def.getElement(), def.getName(), def.getElement(), dependencyNamesCommaSeprataed);
		}
		
		for (String pcm : def.getPostconstrutMethods()) {
			builder.addStatement("$L.$L()", def.getName(), pcm);
		}
		
		builder.addStatement("return $L", def.getName());
		return builder.build();
	}

	private MethodSpec getConstructor() {
		ParameterSpec beanFactoryParameter = ParameterSpec.builder(BeanFactory.class, "beanFactory", Modifier.FINAL).build();
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				//.addParameter(beanFactoryParameter)
				.addStatement("super()")
//				.addCode("super($N);", beanFactoryParameter)
				.build();
	}

	private String getPackage() {
		if(def.getParentConfig() == null) {
			return ((PackageElement) def.getElement().getEnclosingElement()).getQualifiedName().toString();
		}
		return ((PackageElement) def.getParentConfig().getElement().getEnclosingElement()).getQualifiedName().toString();
	}

	private CodeBlock getStaticRegistrationBlock(String className) {
		return CodeBlock.builder()
				.addStatement("$T.beans.add($L::new)", BeanInfoRegister.class, className)
				.build();
	}

	private TypeName getSuperClass() {
		if(isSingleton(def)) {
			return ParameterizedTypeName.get(
					ClassName.get(AbstractSingletonBeanInfo.class),
					getTypeNameForElement()
			);
		}
		return ParameterizedTypeName.get(
				ClassName.get(AbstractBeanInfo.class),
				getTypeNameForElement()
		);
	}

	private String getClassName() {
		return def.getElement().getSimpleName().toString() + "BeanInfo";
	}
}
