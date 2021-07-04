package com.homeprojects.di.generators;

import static com.homeprojects.di.core.Utils.getPackageName;
import static com.homeprojects.di.core.Utils.isSingleton;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.homeprojects.di.annotations.GeneratedBeanInfo;
import com.homeprojects.di.core.BeanDefinition;
import com.homeprojects.di.core.Setter;
import com.homeprojects.di.core.beaninfo.AbstractBeanInfo;
import com.homeprojects.di.core.beaninfo.AbstractSingletonBeanInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
				.addMethod(getConstructor())
				.addMethod(getBuildMethod())
				.addMethod(getNameMethod());
		
		if(!isSingleton(def)) {
			builder.addMethod(getGetInstanceMethod());
			builder.addMethod(getGetTypeMethod());
			builder.addMethod(getGetScopeMethod());
		} else {
			builder.addMethod(getRunDestroysMethod());
		}

		JavaFile file = JavaFile.builder(getPackage(), builder.build()).build();

		try {
			file.writeTo(env.getFiler());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MethodSpec getRunDestroysMethod() {
		return MethodSpec.methodBuilder("runPreDestroys")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addCode(getRunDestroysMethodBody())
				.build();
	}

	private CodeBlock getRunDestroysMethodBody() {
		Builder builder = CodeBlock.builder();
		for(String pdm: def.getPreDestroyMethods()) {
			builder.addStatement("instance.$L()", pdm);
		}
		return builder.build();
	}

	private MethodSpec getNameMethod() {
		return MethodSpec.methodBuilder("name")
					.addAnnotation(Override.class)
					.returns(String.class)
					.addModifiers(Modifier.PUBLIC)
					.addStatement("return $S", def.getName())
					.build();
	}

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
		return TypeName.get(def.getExactType());
	}

	private CodeBlock getBuildMethodBody() {
		Builder builder = CodeBlock.builder();
		
		for (BeanDefinition dependency : def.getDependencies()) {
			builder.addStatement("$T $L = beanFactory.getBean($T.class)", dependency.getExactType(), dependency.getName(), dependency.getElement());
		}
		
		String dependencyNamesCommaSeprataed = def.getDependencies().stream()
				.map(dependency -> dependency.getName())
				.collect(Collectors.joining(", "));
		
		if(def.getParentConfig() != null) {
			BeanDefinition parent = def.getParentConfig();
			builder.addStatement("$T $L = beanFactory.getBean($T.class)", parent.getExactType(), parent.getName(), parent.getElement());
			builder.addStatement("$T $L = $L.$L($L)", def.getExactType(), def.getName(), parent.getName(), def.getConstructor().getSimpleName(), dependencyNamesCommaSeprataed);
		} else {
			builder.addStatement("$T $L = new $T($L)", def.getExactType(), def.getName(), def.getExactType(), dependencyNamesCommaSeprataed);
		}
		builder.add(getAfterInitCode());
		
		builder.addStatement("return $L", def.getName());
		return builder.build();
	}

	private MethodSpec getConstructor() {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addStatement("super()")
				.build();
	}

	private String getPackage() {
		if(def.getParentConfig() == null) {
			return getPackageName(def.getElement());
		}
		return getPackageName(def.getParentConfig().getElement());
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
		String name = def.getName();
		if(name.length() > 1) {
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
		} else {
			name = name.toUpperCase();
		}
		return name + "BeanInfo";
	}

	private CodeBlock getAfterInitCode() {
		Builder builder = CodeBlock.builder();
		
		for(Setter setter: def.getSetters()) {
			builder.add(getSetterCode(setter));
		}
		
		for (String pcm : def.getPostconstrutMethods()) {
			builder.addStatement("$L.$L()", def.getName(),pcm);
		}
		
		return builder.build();
	}

	private CodeBlock getSetterCode(Setter setter) {
		String dependencyListCommaSeprataed = setter.getDependencies()
				.stream()
				.map(dependency -> "beanFactory.getBean($" + dependency.getName() + ":T.class)")
				.collect(Collectors.joining(", "));
		
		Map<String, ?> map = setter.getDependencies()
				.stream()
				.collect(Collectors.toMap(dep -> dep.getName(), dep -> dep.getElement()));
		
		Builder builder = CodeBlock.builder();
		
		builder.add("$L.$L(", def.getName(),setter.getName());
		builder.addNamed(dependencyListCommaSeprataed, map);
		builder.addStatement(")");
		
		return builder.build();
	}
}
