package com.homeprojects.di.core.factory;

public interface BeanFactory extends AutoCloseable {

    <T> T getBean(Class<T> clazz);
}
