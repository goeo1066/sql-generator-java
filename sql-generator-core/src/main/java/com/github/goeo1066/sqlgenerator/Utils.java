package com.github.goeo1066.sqlgenerator;

class Utils {
    public static boolean isBlank(String source) {
        return source == null || source.isBlank();
    }

    public static boolean isNotBlank(String source) {
        return !isBlank(source);
    }
}
