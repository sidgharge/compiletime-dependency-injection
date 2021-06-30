package com.homeprojects.di.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.homeprojects.di.core.beaninfo.BeanInfo;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultBeanFactory implements BeanFactory {

	List<BeanInfo> beanInfos = new ArrayList<>();

    public DefaultBeanFactory() {
    	ServiceLoader<BeanInfo> serviceLoader = ServiceLoader.load(BeanInfo.class);
    	for (BeanInfo beanInfo : serviceLoader) {
    		beanInfo.setBeanFactory(this);
			beanInfos.add(beanInfo);
		}
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        for (BeanInfo beanInfo : beanInfos) {
            if(beanInfo.getType() == clazz) {
                return (T) beanInfo.getInstance();
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
