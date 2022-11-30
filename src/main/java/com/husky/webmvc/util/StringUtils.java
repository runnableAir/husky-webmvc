package com.husky.webmvc.util;

/**
 * 字符串处理工具
 */
public class StringUtils {

    /**
     * 判断字符串是否为空：1.值不存在; 2.值为空字符
     * @param s 判断的字符串
     * @return true 如果“为空”
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
