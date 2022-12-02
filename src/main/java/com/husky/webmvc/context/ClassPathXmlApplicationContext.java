package com.husky.webmvc.context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassPathXmlApplicationContext {

    /**
     * bean元素标签命名
     */
    static final String BEAN_ELEMENT = "bean";

    /**
     * bean元素标签中描述bean名称的属性:name
     */
    static final String BEAN_NAME_ATTR = "name";

    /**
     * bean元素标签中描述bean类名称的属性:class
     */
    static final String BEAN_CLASSNAME_ATTR = "class";

    /**
     * property元素标签命名
     */
    static final String PROPERTY_ELEMENT = "property";

    /**
     * property元素标签中描述property在bean中的引用变量名称的属性:ref
     */
    static final String PROPERTY_NAME_ATTR = "ref";

    /**
     * property元素标签中描述property在bean中引用的bean名称的属性:name
     */
    static final String PROPERTY_BEAN_NAME_ATTR = "name";


    /**
     * bean工厂对象
     */
    private final BeanFactory beanFactory = new BeanFactory();

    /**
     * 创建基于xml配置文件的应用程序上下文。<br>
     * 读取xml中定义的bean及其依赖bean信息，并注册到BeanFactory中。
     * @param configLocation xml配置文件的位置
     */
    public ClassPathXmlApplicationContext(String configLocation) {
        try (InputStream resource = getClass().getResourceAsStream(configLocation)) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(resource);
            // 在map中保存扫描的bean
            Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
            NodeList nodeList = document.getElementsByTagName(BEAN_ELEMENT);
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Element e = (Element) nodeList.item(i);
                String beanName = e.getAttribute(BEAN_NAME_ATTR);
                String beanClassName = e.getAttribute(BEAN_CLASSNAME_ATTR);
                BeanDefinition beanDef;
                // 如果map中还未存在该定义，则直接创建它并保存在map中
                if (!beanDefinitionMap.containsKey(beanName)) {
                    beanDef = new BeanDefinition(beanName, beanClassName);
                    beanDefinitionMap.put(beanName, beanDef);
                } else {
                    // 否则，说明这个bean定义在被声明之前就被前面的bean引用为属性了，
                    // 此时map中的bean定义对象只是被提前创建了，但还未设置`className`属性，所以这里对其进行补充
                    BeanDefinition defWithNoClass = beanDefinitionMap.get(beanName);
                    defWithNoClass.setClassName(beanClassName);
                    beanDef = defWithNoClass;
                }
                if (e.hasChildNodes()) {
                    NodeList childNodes = e.getChildNodes();
                    int nodesLength = childNodes.getLength();
                    Map<String, BeanDefinition> propertyDefinition = new HashMap<>();
                    // 扫描当前bean定义中引用的bean
                    for (int j = 0; j < nodesLength; j++) {
                        Node item = childNodes.item(j);
                        if (item.getNodeType() != Node.ELEMENT_NODE ||
                                !item.getNodeName().equals(PROPERTY_ELEMENT)) {
                            continue;
                        }
                        Element prop = (Element) item;
                        String propName = prop.getAttribute(PROPERTY_NAME_ATTR);
                        String propBeanName = prop.getAttribute(PROPERTY_BEAN_NAME_ATTR);
                        // 从map中获取需要引用的bean的定义，如果没有则提前创建，并保存在map中，
                        // 等后面扫描到该bean定义的时候再对其进行补充
                        beanDefinitionMap.putIfAbsent(propBeanName, new BeanDefinition(propBeanName));
                        propertyDefinition.put(propName, beanDefinitionMap.get(propBeanName));
                    }
                    beanDef.setPropertyDefinition(propertyDefinition);
                }
                beanFactory.registerBean(beanDef);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定名称的bean
     * @param name bean的名称，必须由xml文件声明。
     * @return 对应的bean
     */
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }
}
