package com.jaguar.om.common;


import java.util.Random;

public class Utils {

    private static final Random generator = new Random(1L);
    public static Integer generateKey() {
        return generator.nextInt();
    }
}
