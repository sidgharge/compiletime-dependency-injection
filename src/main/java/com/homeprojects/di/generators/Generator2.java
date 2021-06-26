package com.homeprojects.di.generators;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.homeprojects.di.core.BeanDefinition;

public class Generator2 {

	private final List<BeanDefinition> beans;

	private final ProcessingEnvironment env;

	public Generator2(Queue<BeanDefinition> beans, ProcessingEnvironment env) {
		this.beans = new ArrayList<>(beans);
		this.env = env;
	}

	public void generate() {
		for (BeanDefinition beanDefinition : beans) {
			new BeanInfoGenerator(beanDefinition, env).processBeanDefinition();
		}
	}	


}
