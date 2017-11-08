package com.jaguar.om.common;


import java.util.Random;

public class Utils {

    private static final Random generator = new Random();

    /**
     * For generating the unique application key.
     * @return The Integer value of the generated application key.
     */
    public static Integer generateApplicationKey() {
        return generator.nextInt(9999999);
    }

    /**
     * For generating item number.
     * @return The String value of the generated item number.
     */
    public static String generateItemNumber() {
        return String.valueOf(generator.nextInt(999999999));
    }
}
