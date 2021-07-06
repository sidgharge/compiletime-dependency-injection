package com.homeprojects.di.generators;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;

import com.homeprojects.di.core.BeanDefinition;

public class Generator {

	private final List<BeanDefinition> beans;

	private final ProcessingEnvironment env;

	public Generator(List<BeanDefinition> beans, ProcessingEnvironment env) {
		this.beans = beans;
		this.env = env;
	}

	public void generate() {
		for (int i = 0; i < beans.size(); i++) {
			new BeanInfoGenerator(beans.get(i), i, env).processBeanDefinition();
		}
	}	


}
