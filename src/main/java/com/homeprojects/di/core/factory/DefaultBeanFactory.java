package com.homeprojects.di.core.factory;

import com.homeprojects.di.core.beaninfo.BeanInfo;
import com.homeprojects.di.core.beaninfo.BeanInfoRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultBeanFactory implements BeanFactory {

    List<BeanInfo> beanInfos = new ArrayList<>();

    public DefaultBeanFactory() {
//        BeanInfoRegister.beans.forEach(fn -> beanInfos.add(fn.apply(this)));
    	ServiceLoader<BeanInfo> serviceLoader = ServiceLoader.load(BeanInfo.class);
    	for (BeanInfo beanInfo : serviceLoader) {
    		beanInfo.setBeanFactory(this);
			beanInfos.add(beanInfo);
		}
    	
    	beanInfos.forEach(bi -> bi.onContextInit());
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
