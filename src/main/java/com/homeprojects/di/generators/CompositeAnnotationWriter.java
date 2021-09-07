package com.homeprojects.di.generators;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.homeprojects.di.core.AnnotationRegister;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

public class CompositeAnnotationWriter {

	private static final String PACKAGE_NAME = "com.homeprojects.generated";

	private final ProcessingEnvironment processingEnv;

	private final Set<TypeElement> annotations;

	public CompositeAnnotationWriter(ProcessingEnvironment processingEnv, Set<TypeElement> annotations) {
		this.processingEnv = processingEnv;
		this.annotations = annotations;
	}

	public void write() throws IOException {
		if (annotations.isEmpty()) {
			return;
		}

		List<String> paths = writeClasses();

		FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
				"META-INF/services/" + AnnotationRegister.class.getName());

		try (PrintWriter out = new PrintWriter(resource.openWriter())) {
			for (String path: paths) {
				out.println(path);
			}
		}
	}

	private List<String> writeClasses() throws IOException {
		List<String> paths = new ArrayList<>();
		for (TypeElement typeElement : annotations) {
			paths.add(writeClass(typeElement));
		}
		return paths;
	}

	private String writeClass(TypeElement typeElement) throws IOException {
		String className = typeElement.getQualifiedName().toString().replace(".", "_");
		TypeSpec spec = TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(AnnotationRegister.class)
				.addMethod(getOverrideMethod(typeElement)).build();

		JavaFile file = JavaFile.builder(PACKAGE_NAME, spec).build();

		file.writeTo(processingEnv.getFiler());
		
		return PACKAGE_NAME + "." + className;
	}

	private MethodSpec getOverrideMethod(TypeElement typeElement) {
		TypeName wildcard = WildcardTypeName.subtypeOf(Annotation.class);
		ClassName cls = ClassName.get(Class.class);
		TypeName clsWildcard = ParameterizedTypeName.get(cls, wildcard);

		return MethodSpec.methodBuilder("getAnnotation")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(clsWildcard)
				.addStatement("return $T.class", typeElement).build();
	}
}
