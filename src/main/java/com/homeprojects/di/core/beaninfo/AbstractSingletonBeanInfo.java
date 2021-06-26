package com.homeprojects.di.core.beaninfo;

import com.homeprojects.di.core.beaninfo.AbstractBeanInfo;
import com.homeprojects.di.core.factory.BeanFactory;

public abstract class AbstractSingletonBeanInfo<T> extends AbstractBeanInfo<T> {

    protected T instance;
    
    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public Class<?> getType() {
        return instance.getClass();
    }

    @Override
    public String getScope() {
        return "singleton";
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
    	super.setBeanFactory(beanFactory);
    	this.instance = build();
    }
}
