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
	
	private final ParameterizedTypeName beansMapType;

	public Generator(Queue<BeanDefination> beans, ProcessingEnvironment env) {
		this.beans = new ArrayList<>(beans);
		this.env = env;
			
		this.beansMapType = getBeansMapType();
	}

	public void generate() {
		TypeSpec spec = TypeSpec.
				classBuilder(BEAN_CONTEXT_CLAZZ_NAME)
				.addModifiers(Modifier.PUBLIC)
				.addFields(getFields())
				.addMethod(addInitMethod())
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
				.addCode("return $L;", bean.getName())
				.build();
	}

	private Iterable<FieldSpec> getFields() {
		return beans.stream()
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

	private ParameterizedTypeName getBeansMapType() {
		TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
		TypeName clsWildcard = ParameterizedTypeName.get(ClassName.get(Class.class), wildcard);
		TypeName object = ClassName.get(Object.class);
		ClassName map = ClassName.get(Map.class);
		return ParameterizedTypeName.get(map, clsWildcard, object);
	}

	private MethodSpec addInitMethod() {
		return MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addCode(writeObjects())
				.build();
	}

	private CodeBlock writeObjects() {
		Builder builder = CodeBlock.builder();
		for (int i = 0; i < beans.size(); i++) {
			BeanDefination dependency = beans.get(i);
			TypeElement type = dependency.getElement();
			String paramertsInString = getParametersSeparatedByComma(dependency);
			builder.addStatement(CodeBlock.of("this.$L = new $T($L)", dependency.getName(), type, paramertsInString));
		}
		return builder.build();
	}

	private String getParametersSeparatedByComma(BeanDefination dependency) {
		return dependency.getDependencies()
				.stream()
				.map(bean -> bean.getName() + "()")
				.collect(Collectors.joining(", "));
	}

}
