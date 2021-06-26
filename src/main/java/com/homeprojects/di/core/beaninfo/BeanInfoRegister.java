package com.homeprojects.di.core.beaninfo;

import com.homeprojects.di.core.beaninfo.BeanInfo;
import com.homeprojects.di.core.factory.BeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BeanInfoRegister {

    public static List<Function<BeanFactory, BeanInfo>> beans = new ArrayList<>();
    
    public BeanInfoRegister() {
		// TODO Auto-generated constructor stub
	}
}