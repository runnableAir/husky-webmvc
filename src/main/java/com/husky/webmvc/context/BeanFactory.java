package com.husky.webmvc.context;

import com.husky.webmvc.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

    /**
     * 存储所有bean的map，键为bean的名称name，值为bean对象
     */
    private final Map<String, Object> beanMap;

    /**
     * 存储所有的bean定义的map，键为bean的名称name，值为bean定义对象
     */
    private final Map<String, BeanDefinition> beanDefinitionMap;


    public BeanFactory() {
        beanMap = new HashMap<>();
        beanDefinitionMap = new HashMap<>();
    }

    /**
     * 向beanFactory注册一个bean，传入的是对应的bean定义对象
     * @param beanDefinition bean定义对象
     */
    public void registerBean(BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }

    /**
     * 根据bean的名称获得对象。
     * 如果beanMap中有该对象，则直接返回该对象，
     * 否则，尝试创建该bean。如果创建的bean没有相应的BeanDefinition对象，则无法创建。
     * 新创建的bean通过无参构造方法创建，其中的属性，或者说依赖还未设置。
     * bean的属性由injectBean方法进行设置，其中的属性，即bean，如果也未创建，则递归进行创建。
     *
     * @param name bean的名称
     * @return bean实例
     */
    public Object getBean(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("bean name cannot be explicit:" + name);
        }
        if (beanMap.containsKey(name)) {
            return beanMap.get(name);
        }
        if (!beanDefinitionMap.containsKey(name)) {
            throw new RuntimeException("no bean definition with bean name: " + name);
        }
        // start create bean
        BeanDefinition beanDef = beanDefinitionMap.get(name);
        Object bean = createBean(beanDef);
        injectBean(bean, beanDef);
        beanMap.put(name, bean);
        return bean;
    }

    /**
     * 对bean的属性进行注入（依赖注入）
     * 该方法是进行属性注入流程的开始，实际注入过程由doInject方法实现
     *
     * @param bean 当前被注入的bean
     * @param beanDef 当前被注入的bean的定义对象——BeanDefinition
     */
    private void injectBean(Object bean, BeanDefinition beanDef) {
        Map<String, Object> cacheBeanMap = new HashMap<>();
        try {
            doInject(bean, beanDef, cacheBeanMap);
        } catch (Exception e) {
            throw new RuntimeException("bean injecting failed for bean: " + beanDef.getClassName(), e);
        } finally {
            cacheBeanMap.clear();
        }
    }

    /**
     * 对bean进行属性注入
     * 该方法按递归的方式的逐步创建、获取和注入依赖的bean对象，最终完成对目标bean对象的注入
     *
     * @param instance 当前被注入的bean实例
     * @param beanDef 当前被注入的bean的定义对象
     * @param cacheBeanMap 在当前bean处理前所缓存的bean，目的是防止循环依赖导致的无限递归。
     */
    private void doInject(Object instance, BeanDefinition beanDef, Map<String, Object> cacheBeanMap) {
        cacheBeanMap.put(beanDef.getName(), instance);
        Map<String, BeanDefinition> propertyDefinition = beanDef.getPropertyDefinition();
        if (propertyDefinition == null) {
            System.out.println("no bean need to be injected to this bean: " + beanDef.getName());
            return;
        }
        Class<?> instanceClass = instance.getClass();
        propertyDefinition.forEach((propName, propBeanDef) -> {
            String propBeanName = propBeanDef.getName();
            try {
                Field field = instanceClass.getDeclaredField(propName);
                Object bean;
                // get bean that is required by current bean
                // first try to get it from `beanMap`
                // if no, try it by `cacheBeanMap`
                // if no, it needs to be created & injected recursively
                // and to be exactly saved in `beanMap`
                if (beanMap.containsKey(propBeanName)) {
                    bean = beanMap.get(propBeanName);
                } else if (cacheBeanMap.containsKey(propBeanName)) {
                    bean = cacheBeanMap.get(propBeanName);
                } else {
                    bean = createBean(propBeanDef);
                    doInject(bean, propBeanDef, cacheBeanMap);
                    beanMap.put(propBeanName, bean);
                }
                // then set it to the field corresponding to the `propName`.
                field.setAccessible(true);
                field.set(instance, bean);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 创建一个bean，即完成一次实例化，
     * 该方法将根据bean定义的className属性来加载Class，然后通过反射进行实例化
     *
     * @param beanDef bean定义对象
     * @return bean实例对象
     */
    private Object createBean(BeanDefinition beanDef) {
        String className = beanDef.getClassName();
        try {
            Class<?> aClass = Class.forName(className);
            return aClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("bean instantiation failed!", e);
        }
    }
}
