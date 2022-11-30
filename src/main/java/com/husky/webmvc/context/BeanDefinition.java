package com.husky.webmvc.context;

import java.util.Map;

/**
 * bean定义
 */
public class BeanDefinition {

    /**
     * bean的名称，或者叫做id，用于唯一确定一个bean
     */
    private final String name;

    /**
     * bean的类名称，用于确定bean实例的类型
     */
    private final String className;

    /**
     * bean实例包含的属性，描述当前bean的依赖
     */
    private Map<String, BeanDefinition> propertyDefinition;

    public BeanDefinition(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, BeanDefinition> getPropertyDefinition() {
        return propertyDefinition;
    }

    public void setPropertyDefinition(Map<String, BeanDefinition> propertyDefinition) {
        this.propertyDefinition = propertyDefinition;
    }

    @Override
    public String toString() {
        return "Bean[name=" + name + ", className=" + className + "]";
    }
}
