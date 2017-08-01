package com.jaguar.om.common;


import java.util.Random;

public class Utils {

    private static final Random generator = new Random();
    public static Integer generateKey() {
        return generator.nextInt(9999999);
    }
}
