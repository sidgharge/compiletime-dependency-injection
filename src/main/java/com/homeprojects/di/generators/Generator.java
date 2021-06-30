package com.homeprojects.di.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.processing.ProcessingEnvironment;

import com.homeprojects.di.core.BeanDefinition;

public class Generator {

	private final List<BeanDefinition> beans;

	private final ProcessingEnvironment env;

	public Generator(Queue<BeanDefinition> beans, ProcessingEnvironment env) {
		this.beans = new ArrayList<>(beans);
		this.env = env;
	}

	public void generate() {
		for (BeanDefinition beanDefinition : beans) {
			new BeanInfoGenerator(beanDefinition, env).processBeanDefinition();
		}
	}	


}
