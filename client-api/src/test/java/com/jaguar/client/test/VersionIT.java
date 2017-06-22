package com.jaguar.client.test;

import com.jaguar.om.test.BaseTestCase;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class VersionIT extends BaseTestCase {

    @Test
    public void testAssertClientPort() throws Exception {
        Assert.assertTrue(CLIENT_PORT.equals("18080"));
    }
}
