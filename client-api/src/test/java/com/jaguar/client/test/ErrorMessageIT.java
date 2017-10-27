package com.jaguar.client.test;

import com.jaguar.exception.ErrorMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ErrorMessageIT {

    @Test
    public void testJSONErrorMessage() throws Exception {
        final String errorJSON = ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build();
        Assert.assertNotNull(errorJSON);
    }
}
