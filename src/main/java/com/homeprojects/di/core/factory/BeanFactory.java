package com.homeprojects.di.core.factory;

import java.util.List;

public interface BeanFactory extends AutoCloseable {

    <T> T getBean(Class<T> clazz);
    
    <T> T getBean(String name, Class<T> clazz);
    
    <T> List<T> getBeans(Class<T> clazz);
}
