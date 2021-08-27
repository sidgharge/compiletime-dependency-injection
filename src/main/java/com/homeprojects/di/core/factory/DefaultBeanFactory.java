package com.homeprojects.di.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;

import com.homeprojects.di.core.BeanException;
import com.homeprojects.di.core.beaninfo.BeanInfo;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultBeanFactory implements BeanFactory {

	List<BeanInfo> beanInfos = new ArrayList<>();

    public DefaultBeanFactory() {
    	ServiceLoader<BeanInfo> serviceLoader = ServiceLoader.load(BeanInfo.class);
    	Queue<BeanInfo> temp = new PriorityQueue<>((b1, b2) -> b1.getDependecyIndex() - b2.getDependecyIndex()); 
    	for (BeanInfo beanInfo : serviceLoader) {
    		temp.add(beanInfo);
		}
    	for (BeanInfo beanInfo : temp) {
    		beanInfo.setBeanFactory(this);
    		beanInfos.add(beanInfo);
		}
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
    	Object bean = null;
        for (BeanInfo beanInfo : beanInfos) {
            if(beanInfo.getType() == clazz || clazz.isAssignableFrom(beanInfo.getType())) {
            	if(bean != null) {
            		throw new BeanException("Multiple beans of this type " + clazz + ". Consider providing name for the bean.");
            	}
                bean = beanInfo.getInstance();
            }
        }
        
        if(bean != null) {
        	return (T) bean;
        }
        throw new BeanException("No bean of type " + clazz + " exists");
    }
    
    @Override
    public <T> T getBean(String name, Class<T> clazz) {
    	for (BeanInfo beanInfo : beanInfos) {
            if(beanInfo.name().equals(name) && beanInfo.getType() == clazz) {
                return (T) beanInfo.getInstance();
            }
        }
        throw new BeanException(String.format("No bean exists with name '%s' and %s", name, clazz));
    }

    @Override
    public void close() throws Exception {
    	for (BeanInfo beanInfo : beanInfos) {
    		beanInfo.runPreDestroys();
    	}
    }
}
