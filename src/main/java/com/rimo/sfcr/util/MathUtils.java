package com.rimo.sfcr.util;

public class MathUtils {
    public static int ceilDiv(int a, int b) {
        return a / b + (a % b > 0 ? 1 : 0);
    }

    public static int ceil8(int a) {
        return (a + 7) & ~7;
    }
}
