package com.github.ningasekiro.util;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 单例类<br>
 * 提供单例对象的统一管理，当调用get方法时，如果对象池中存在此对象，返回此对象，否则创建新对象返回<br>
 *
 * @author loolly
 */
public final class Singleton {

    private static final ConcurrentHashMap<String, Object> POOL = new ConcurrentHashMap<>();

    private Singleton() {
    }

    /**
     * 获得指定类的单例对象<br>
     * 对象存在于池中返回，否则创建，每次调用此方法获得的对象为同一个对象<br>
     * 注意：单例针对的是类和参数，也就是说只有类、参数一致才会返回同一个对象
     *
     * @param <T>   单例对象类型
     * @param clazz 类
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        final String key = clazz.getName();
        return (T) POOL.computeIfAbsent(key, k -> {
            try {
                // 使用 ConstructorUtils 通过反射创建对象
                return BeanUtils.instantiateClass(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 将已有对象放入单例中，其Class做为键
     *
     * @param obj 对象
     * @since 4.0.7
     */
    public static void put(Object obj) {
        Assert.notNull(obj, "Bean object must be not null !");
        put(obj.getClass().getName(), obj);
    }

    /**
     * 将已有对象放入单例中，key做为键
     *
     * @param key 键
     * @param obj 对象
     * @since 5.3.3
     */
    public static void put(String key, Object obj) {
        POOL.put(key, obj);
    }

    /**
     * 判断某个类的对象是否存在
     *
     * @param clazz  类
     * @param params 构造参数
     * @return 是否存在
     */
    public static boolean exists(Class<?> clazz, Object... params) {
        if (null != clazz) {
            final String key = clazz.getName();
            return POOL.containsKey(key);
        }
        return false;
    }

    /**
     * 获取单例池中存在的所有类
     *
     * @return 非重复的类集合
     */
    public static Set<Class<?>> getExistClass() {
        return POOL.values().stream().map(Object::getClass).collect(Collectors.toSet());
    }

    /**
     * 移除指定Singleton对象
     *
     * @param clazz 类
     */
    public static void remove(Class<?> clazz) {
        if (null != clazz) {
            remove(clazz.getName());
        }
    }

    /**
     * 移除指定Singleton对象
     *
     * @param key 键
     */
    public static void remove(String key) {
        POOL.remove(key);
    }

    /**
     * 清除所有Singleton对象
     */
    public static void destroy() {
        POOL.clear();
    }

}
