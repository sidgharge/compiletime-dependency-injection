package com.homeprojects.di.core.beaninfo;

import com.homeprojects.di.core.factory.BeanFactory;

public abstract class AbstractBeanInfo<T> implements BeanInfo<T> {

    protected BeanFactory beanFactory;

    public AbstractBeanInfo() {
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
    	this.beanFactory = beanFactory;
    }
    
    @Override
    public String toString() {
    	return this.getClass().getSimpleName() + ": " + getDependecyIndex();
    }
}
