package com.jaguar.client.test;

import com.jaguar.om.test.BaseTestCase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.util.Map;

/**
 * Performs the authorization & authentication (if required)
 * http://localhost:8080/api/oauth/authorize?response_type=json&client_id=1095369
 * &redirect_uri=http://localhost:8080&scope=seller&device_uid=GOOGLECHROME
 */
@Test(groups="authorization")
public class AuthorizationTest extends BaseTestCase {

    @Test
    @Rollback(value = false)
    public void testAccessToken() throws Exception {
        //COmplete the authorization process for ashish.raghavan@google.com first.
        doAuthorizationAndAuthentication("ashish.raghavan@google.com","12345","true",
                "http://localhost:8080","1095369","GOOGLECHROME","seller");
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Assert.assertTrue(!Strings.isNullOrEmpty(getAccessToken()));
        Assert.assertTrue(!Strings.isNullOrEmpty(getEmail()));
        final HttpGet httpGet = new HttpGet("http://localhost:8080/api/user/"+(getEmail().replace("@","%40")));
        httpGet.addHeader("Authorization","Bearer "+getAccessToken());
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value());
        final String responseString = EntityUtils.toString(httpResponse.getEntity());
        Assert.assertNotNull(responseString);
        final Map<String,Object> responseMap = mapper.readValue(responseString,typeMapStringObject);
        Assert.assertNotNull(responseMap.get("email"));
    }
}
