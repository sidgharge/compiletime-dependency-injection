package com.homeprojects.di.core.beaninfo;

import com.homeprojects.di.core.factory.BeanFactory;

public interface BeanInfo<T> {
	
	String name();

    T build();

    T getInstance();

    Class<?> getType();

    String getScope();
    
    default void runPreDestroys() {
    }
    
    void setBeanFactory(BeanFactory beanFactory);
    
    default boolean isSingleton() {
        return "singleton".equals(getScope());
    }
}
