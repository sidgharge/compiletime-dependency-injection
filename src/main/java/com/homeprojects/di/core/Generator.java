//package com.homeprojects.di.core;
//
//import static com.homeprojects.di.core.Utils.isSingleton;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//import java.util.stream.Collectors;
//
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//
//import com.homeprojects.di.core.factory.BeanFactory;
//import com.squareup.javapoet.CodeBlock;
//import com.squareup.javapoet.CodeBlock.Builder;
//import com.squareup.javapoet.FieldSpec;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.TypeName;
//import com.squareup.javapoet.TypeSpec;
//
//public class Generator {
//
//	private static final String BEAN_CONTEXT_CLAZZ_NAME = "DeafultBeanFactory";
//
//	private static final String BEAN_CONTEXT_PACKAGE_NAME = "com.homeprojects.generated";
//
//	private final List<BeanDefinition> beans;
//
//	private final ProcessingEnvironment env;
//
//	public Generator(Queue<BeanDefinition> beans, ProcessingEnvironment env) {
//		this.beans = new ArrayList<>(beans);
//		this.env = env;
//	}
//
//	public void generate() {
//		TypeSpec spec = TypeSpec.
//				classBuilder(BEAN_CONTEXT_CLAZZ_NAME)
//				.addSuperinterface(BeanFactory.class)
//				.addModifiers(Modifier.PUBLIC)
//				.addField(getIsClosedField())
//				.addFields(getSingletonFields())
//				.addMethod(getConstructor())
//				.addMethod(getCloseMethod())
//				.addMethods(getAccessors())
//				.build();
//		
//		JavaFile file = JavaFile.builder(BEAN_CONTEXT_PACKAGE_NAME, spec).build();
//		
//		try {
//			file.writeTo(env.getFiler());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private MethodSpec getCloseMethod() {
//		return MethodSpec.methodBuilder("close")
//				.addAnnotation(Override.class)
//				.addModifiers(Modifier.PUBLIC)
//				.addCode(getCloseMethodBody())
//				.returns(void.class)
//				.build();
//	}
//
//	private CodeBlock getCloseMethodBody() {
//		Builder builder = CodeBlock.builder();
//		
//		builder.beginControlFlow("if(isClosed)");
//		builder.addStatement("return");
//		builder.endControlFlow();
//		
//		beans.stream()
//				.filter(Utils::isSingleton)
//				.map(bean -> getCloseCallStatement(bean))
//				.forEach(cb -> builder.add(cb));
//		
//		builder.addStatement("isClosed = true");
//		return builder.build();
//	}
//
//	private CodeBlock getCloseCallStatement(BeanDefinition bean) {
//		Builder builder = CodeBlock.builder();
//		
//		for(String preDestroyMethodName: bean.getPreDestroyMethods()) {
//			builder.addStatement("this.$L.$L()", bean.getName(), preDestroyMethodName);
//		}
//		
//		return builder.build();
//	}
//
//	private FieldSpec getIsClosedField() {
//		return FieldSpec
//				.builder(TypeName.BOOLEAN, "isClosed", Modifier.PRIVATE)
//				.initializer("false")
//				.build();
//	}
//
//	private Iterable<MethodSpec> getAccessors() {
//		return beans.stream()
//				.map(bean -> getAcessor(bean))
//				.collect(Collectors.toList());
//	}
//	
//	private MethodSpec getAcessor(BeanDefinition bean) {
//		return MethodSpec.methodBuilder(bean.getName())
//				.addModifiers(Modifier.PUBLIC)
//				.returns(asTypeName(bean.getElement()))
//				.addCode(getAccessorBody(bean))
//				.build();
//	}
//
//	private CodeBlock getAccessorBody(BeanDefinition bean) {
//		if(isSingleton(bean)) {
//			return CodeBlock.of("return $L;", bean.getName());
//		}
//		return getPrototypeAccesorBody(bean);
//	}
//
//	private CodeBlock getPrototypeAccesorBody(BeanDefinition bean) {
//		String paramertsInString = getParametersSeparatedByComma(bean);
//		
//		List<CodeBlock> codeBlocks = new ArrayList<>();
//		CodeBlock cb1 = null;
//		if(bean.getParentConfig() == null) {
//			cb1 = CodeBlock.of("$T temp = new $T($L);", bean.getElement(), bean.getElement(), paramertsInString);			
//		} else {
//			cb1 = CodeBlock.of("$T temp = $L.$L($L);",
//					bean.getElement(), bean.getParentConfig().getName(),
//					bean.getConstructor().getSimpleName().toString(),
//					paramertsInString);
//		}
//		codeBlocks.add(cb1);
//		for(String postconstructMethod : bean.getPostconstrutMethods()) {
//			CodeBlock cb2 = CodeBlock.of("temp.$L();", postconstructMethod);
//			codeBlocks.add(cb2);
//		}
//		CodeBlock cb3 = CodeBlock.of("return temp;", bean.getElement(), paramertsInString);
//		codeBlocks.add(cb3);
//		return CodeBlock.join(codeBlocks, "\n");
//	}
//
//	private Iterable<FieldSpec> getSingletonFields() {
//		return beans.stream()
//			.filter(Utils::isSingleton)
//			.map(bean -> getSingletonField(bean))
//			.collect(Collectors.toList());
//	}
//
//	private FieldSpec getSingletonField(BeanDefinition bean) {
//		return FieldSpec
//			.builder(asTypeName(bean.getElement()), bean.getName(), Modifier.PRIVATE)
//		    .build();
//	}
//	
//	private TypeName asTypeName(TypeElement element) {
//		return TypeName.get(element.asType());
//	}
//
//	private MethodSpec getConstructor() {
//		return MethodSpec
//				.constructorBuilder()
//				.addModifiers(Modifier.PUBLIC)
//				.addCode(getConstructorBody())
//				.build();
//	}
//
//	private CodeBlock getConstructorBody() {
//		Builder builder = CodeBlock.builder();
//		beans.stream()
//			.filter(Utils::isSingleton)
//			.flatMap(bean -> getConstrutorBeanStatement(bean).stream())
//			.forEach(cb -> builder.addStatement(cb));
//		return builder.build();
//	}
//
//	private List<CodeBlock> getConstrutorBeanStatement(BeanDefinition bean) {
//		TypeElement type = bean.getElement();
//		String paramertsInString = getParametersSeparatedByComma(bean);
//		
//		List<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();
//		if(bean.getParentConfig() == null) {
//			codeBlocks.add(CodeBlock.of("this.$L = new $T($L)", bean.getName(), type, paramertsInString));
//		} else {
//			codeBlocks.add(CodeBlock.of("this.$L = $L.$L($L)", bean.getName(), bean.getParentConfig().getName(), bean.getConstructor().getSimpleName().toString(), paramertsInString));
//		}
//		for(String postconstructMethod: bean.getPostconstrutMethods()) {
//			codeBlocks.add(CodeBlock.of("this.$L.$L()", bean.getName(), postconstructMethod));
//		}
//		return codeBlocks;
//	}
//
//	private String getParametersSeparatedByComma(BeanDefinition dependency) {
//		return dependency.getDependencies()
//				.stream()
//				.map(bean -> bean.getName() + "()")
//				.collect(Collectors.joining(", "));
//	}
//
//}
