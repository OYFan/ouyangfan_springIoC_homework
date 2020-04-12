package com.lagou.edu.test;

import com.lagou.edu.annotation.*;
import com.lagou.edu.factory.AnnotationBeanFactory;
import com.lagou.edu.factory.ProxyFactory;
import com.lagou.edu.service.TransferService;
import com.mysql.jdbc.StringUtils;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.*;

public class MyTest {

    @Test
    public void test2() throws Exception {
        Map<String, Object> map = new HashMap<>();
        //使用反射包
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("com.lagou.edu")
                .addScanners(new SubTypesScanner()) // 添加子类扫描工具
                .addScanners(new FieldAnnotationsScanner()) // 添加 属性注解扫描工具
                .addScanners(new MethodAnnotationsScanner()) // 添加 方法注解扫描工具
                .addScanners(new MethodParameterScanner()) // 添加方法参数扫描工具
        );
        //获取指定包下含有注解的类
        Set<Class<?>> component = reflections.getTypesAnnotatedWith(Component.class, true);
        Set<Class<?>> repository = reflections.getTypesAnnotatedWith(Repository.class, true);
        Set<Class<?>> controller = reflections.getTypesAnnotatedWith(Controller.class, true);
        Set<Class<?>> service = reflections.getTypesAnnotatedWith(Service.class, true);
        component.addAll(repository);
        component.addAll(controller);
        component.addAll(service);
        //合并后遍历
        for (Class<?> aClass : component) {
            //获取注解中的value
            String annotationValue = null;
            Component com = aClass.getAnnotation(Component.class);
            if (!Objects.isNull(com) && !StringUtils.isEmptyOrWhitespaceOnly(com.value())) {
                annotationValue = com.value();
            }
            Repository rep = aClass.getAnnotation(Repository.class);
            if (!Objects.isNull(rep) && !StringUtils.isEmptyOrWhitespaceOnly(rep.value())) {
                annotationValue = rep.value();
            }
            Controller con = aClass.getAnnotation(Controller.class);
            if (!Objects.isNull(con) && !StringUtils.isEmptyOrWhitespaceOnly(con.value())) {
                annotationValue = con.value();
            }
            Service ser = aClass.getAnnotation(Service.class);
            if (!Objects.isNull(ser) && !StringUtils.isEmptyOrWhitespaceOnly(ser.value())) {
                annotationValue = ser.value();
            }
            //获取类名  将首字母转小写
            String simpleClassName = aClass.getSimpleName();
            simpleClassName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            //若注解Value不为空，则以Value为BeanId，否则为首字母小写的类名
            map.put(annotationValue == null ? simpleClassName : annotationValue, aClass.newInstance());
        }

        //遍历bean工厂，进行依赖注入
        for (String beanId : map.keySet()) {
            Object instance = map.get(beanId);
            //获取所有属性
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                //获取指定注入beanId
                Qualifier qualifier = field.getAnnotation(Qualifier.class);
                if (!Objects.isNull(autowired)) {
                    Class<?> typeClazz = field.getType();
                    //注入类型为接口类型  寻找实现类
                    if (typeClazz.isInterface()) {
                        Set<Class<?>> subTypesOfField = reflections.getSubTypesOf((Class<Object>) typeClazz);
                        //若接口实现类不止一个且未指定注入beanId
                        if (subTypesOfField.size() > 1 && (Objects.isNull(qualifier) || StringUtils.isEmptyOrWhitespaceOnly(qualifier.value()))) {
                            throw new Exception("接口实现类不止一个且为指定注入beanId");
                        } else if (!Objects.isNull(qualifier) && !StringUtils.isEmptyOrWhitespaceOnly(qualifier.value())) {
                            //指定有beanId  则直接取出对应实例进行赋值
                            String qualifierValue = qualifier.value();
                            Object fieldValue = map.get(qualifierValue);
                            field.setAccessible(true);
                            field.set(instance, fieldValue);
                        } else if (Objects.isNull(qualifier) || StringUtils.isEmptyOrWhitespaceOnly(qualifier.value())) {
                            //未指定beanId且实现类只有一个  自动按类型装配
                            for (Class<?> aClass : subTypesOfField) {
                                field.setAccessible(true);
                                Object obj = aClass.newInstance();
                                setNestField(aClass,obj);
                                field.set(instance, obj);
                                continue;
                            }
                        }
                    }else {//注入类型不为接口
                        if (!Objects.isNull(qualifier) && !StringUtils.isEmptyOrWhitespaceOnly(qualifier.value())) {
                            //指定有beanId  则直接取出对应实例进行赋值
                            String qualifierValue = qualifier.value();
                            Object fieldValue = map.get(qualifierValue);
                            field.setAccessible(true);
                            field.set(instance, fieldValue);
                        } else if (Objects.isNull(qualifier) || StringUtils.isEmptyOrWhitespaceOnly(qualifier.value())) {
                            //未指定beanId且实现类只有一个  自动按类型装配
                            field.setAccessible(true);
                            Object obj = typeClazz.newInstance();
                            setNestField(typeClazz,obj);
                            field.set(instance, obj);
                        }
                    }
                }
            }
        }
        Set<Class<?>> transactionalClass = reflections.getTypesAnnotatedWith(Transactional.class, true);
        for (Class<?> aClass : transactionalClass) {
            for (String beanId : map.keySet()) {
                Object beanObj = map.get(beanId);
                Class<?> beanClass = beanObj.getClass();
                if (aClass == map.get(beanId).getClass()){
                    ProxyFactory proxyFactory = (ProxyFactory)map.get("proxyFactory");
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    map.put(beanId,interfaces.length > 0 ?  proxyFactory.getJdkProxy(beanObj) : proxyFactory.getCglibProxy(beanObj));
                }
            }
        }
        TransferService transferService = (TransferService) map.get("transferService") ;
        transferService.transfer("6029621011000","6029621011001",111);

    }

    //传入需要待注入的属性  和对象
    public static Object setNestField(Class<?> clazz,Object obj) throws IllegalAccessException, InstantiationException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields){
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (!Objects.isNull(autowired)){
                Class<?> type = field.getType();
                Object instance = type.newInstance();
                setNestField(type,instance);
                field.setAccessible(true);
                field.set(obj,instance);
                return obj;
            }
        }
        return obj;
    }
}
