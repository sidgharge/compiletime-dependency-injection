package com.homeprojects.di.core.beaninfo;

import com.homeprojects.di.core.factory.BeanFactory;

public interface BeanInfo<T> {

    T build();

    T getInstance();

    Class<?> getType();

    String getScope();
    
    void setBeanFactory(BeanFactory beanFactory);
    
    void onContextInit();

    default boolean isSingleton() {
        return "singleton".equals(getScope());
    }
}
