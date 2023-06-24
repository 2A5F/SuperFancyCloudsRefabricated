package com.rimo.sfcr.util;

public class MathUtils {
    public static int IntCeilDiv(int a, int b) {
        return a / b + (a % b > 0 ? 1 : 0);
    }
}
