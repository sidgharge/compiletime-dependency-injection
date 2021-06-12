package com.homeprojects.di.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

public class Generator {

	private static final String BEAN_CONTEXT_CLAZZ_NAME = "BeanFactory";

	private static final String BEAN_CONTEXT_PACKAGE_NAME = "com.homeprojects.generated";

	private final List<BeanDefination> beans;

	private final ProcessingEnvironment env;

	public Generator(Queue<BeanDefination> beans, ProcessingEnvironment env) {
		this.beans = new ArrayList<>(beans);
		this.env = env;
	}

	public void generate() {
		TypeSpec spec = TypeSpec.
				classBuilder(BEAN_CONTEXT_CLAZZ_NAME)
				.addModifiers(Modifier.PUBLIC)
				.addFields(getFields())
				.addMethod(getConstructor())
				.addMethods(getAccessors())
				.build();
		
		JavaFile file = JavaFile.builder(BEAN_CONTEXT_PACKAGE_NAME, spec).build();
		
		try {
			file.writeTo(env.getFiler());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Iterable<MethodSpec> getAccessors() {
		return beans.stream()
				.map(bean -> getAcessor(bean))
				.collect(Collectors.toList());
	}
	
	private MethodSpec getAcessor(BeanDefination bean) {
		return MethodSpec.methodBuilder(bean.getName())
				.addModifiers(Modifier.PUBLIC)
				.returns(asTypeName(bean.getElement()))
				.addCode(getAccessorBody(bean))
				.build();
	}

	private CodeBlock getAccessorBody(BeanDefination bean) {
		if(bean.getScope().equals("singleton")) {
			return CodeBlock.of("return $L;", bean.getName());
		}
		String paramertsInString = getParametersSeparatedByComma(bean);
		return CodeBlock.of("return new $T($L);", bean.getElement(), paramertsInString);
	}

	private Iterable<FieldSpec> getFields() {
		return beans.stream()
			.filter(bean -> bean.getScope().equals("singleton"))
			.map(bean -> getField(bean))
			.collect(Collectors.toList());
	}

	private FieldSpec getField(BeanDefination bean) {
		return FieldSpec
			.builder(asTypeName(bean.getElement()), bean.getName(), Modifier.PRIVATE)
		    .build();
	}
	
	private TypeName asTypeName(TypeElement element) {
		return TypeName.get(element.asType());
	}

	private MethodSpec getConstructor() {
		return MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addCode(getConstructorBody())
				.build();
	}

	private CodeBlock getConstructorBody() {
		Builder builder = CodeBlock.builder();
		beans.stream()
			.filter(bean -> bean.getScope().equals("singleton"))
			.forEach(bean -> getConstrutorBeanStatement(builder, bean));
		return builder.build();
	}

	private void getConstrutorBeanStatement(Builder builder, BeanDefination bean) {
		TypeElement type = bean.getElement();
		String paramertsInString = getParametersSeparatedByComma(bean);
		builder.addStatement(CodeBlock.of("this.$L = new $T($L)", bean.getName(), type, paramertsInString));
	}

	private String getParametersSeparatedByComma(BeanDefination dependency) {
		return dependency.getDependencies()
				.stream()
				.map(bean -> bean.getName() + "()")
				.collect(Collectors.joining(", "));
	}

}
