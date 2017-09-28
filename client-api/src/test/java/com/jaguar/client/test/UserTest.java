package com.jaguar.client.test;

import com.jaguar.om.test.BaseTestCase;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

@Test
public class UserTest extends BaseTestCase {

    private final Map<String,String> formMap = ImmutableMap.<String,String>builder()
            .put("username","ashishraghavan13687@gmail.com")
            .put("password","12345")
            .put("device_uid","iOS6sPlus-A1687")
            .put("model","iPhone6sPlus")
            .put("first_name","Ashish")
            .put("last_name","Raghavan")
            .put("phone","4082216275")
            .put("client_id","1095369")
            .put("api_version","15")
            .build();
    @Test
    @Rollback(value = false)
    public void registerUser() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final MultipartEntityBuilder userEntityBuilder = MultipartEntityBuilder.create();
        for(String formKey : formMap.keySet()) {
             userEntityBuilder.addTextBody(formKey,formMap.get(formKey));
        }
        final HttpPost httpPost = new HttpPost("http://localhost:8080/api/user/");
        httpPost.setEntity(userEntityBuilder.build());
        final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED);
    }
}
