package com.github.goeo1066.sqlgenerator;

public class Util {
    public static boolean isBlank(String s) {
        return Strings.isBlank(s);
    }

    public static class Strings {
        public static boolean isBlank(String s) {
            return s == null || s.isBlank();
        }

        public static boolean isNotBlank(String s) {
            return !isBlank(s);
        }
    }
}
