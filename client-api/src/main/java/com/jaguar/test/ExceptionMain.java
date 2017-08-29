package com.jaguar.test;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ExceptionMain {
    public static void main(String[] args) throws Exception {
        final String currentTime = String.valueOf(System.currentTimeMillis());
        System.out.println(currentTime);
        final Integer clientId = 2190832;
        final String secret = "7a5e9fc6-290b-4c97-8386-67237414f469";

        final Mac hmac = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(),"HmacSHA256");
        hmac.init(secretKeySpec);
        System.out.println(Base64.encodeBase64String(hmac.doFinal(currentTime.getBytes())));
    }
}
