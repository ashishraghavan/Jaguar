package com.jaguar.test;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ExceptionMain {

    /*f*/
    public static void main(String[] args) throws Exception {
        final String currentTime = String.valueOf(System.currentTimeMillis());
        System.out.println(currentTime);
        final Integer clientId = 1095369;
        final String secret = "f6b2d7a9-ec5e-496e-9155-2b5127e38db5";

        final Mac hmac = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(),"HmacSHA256");
        hmac.init(secretKeySpec);
        System.out.println(Base64.encodeBase64String(hmac.doFinal(currentTime.getBytes())));
    }
}
