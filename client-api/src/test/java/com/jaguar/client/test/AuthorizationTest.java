package com.jaguar.client.test;

import com.jaguar.om.test.BaseTestCase;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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

    /**
     *         formData.set('username',username);
     formData.set('password',password);
     formData.set('auth_flow',oauth2_flow);
     formData.set('redirect_uri',redirect_uri);
     formData.set('client_id',clientId);
     formData.set('device_uid',deviceUid);
     formData.set('scopes',scopes);
     * throws Exception
     * Location: http://localhost:8080/consent.html?redirect_uri=http://localhost:8080&oauth2_flow=true&client_id=1095369&authorization_code=395caddc-d276-4a66-b098-37e85c545dc8&device_uid=GOOGLECHROME&scopes=seller
     */
    private String accessToken = null;
    private String email = null;
    private final String clientSecret = "f6b2d7a9-ec5e-496e-9155-2b5127e38db5";
    @Test
    @SuppressWarnings("unchecked")
    @Rollback(value = false)
    public void testRedirectionToLoginPage() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAuthCaching().disableRedirectHandling().build();
        final HttpGet httpGet = new HttpGet("http://localhost:8080/api/oauth/authorize?response_type=json&client_id=1095369" +
                "&redirect_uri=http://localhost:8080&scope=seller&device_uid=GOOGLECHROME");
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.TEMPORARY_REDIRECT.value());
        final Header locationHeader = httpResponse.getFirstHeader("Location");
        Assert.assertNotNull(locationHeader);
        //Assertion that the location value from the header is not null.
        Assert.assertNotNull(locationHeader.getValue());
        //Since we don't have the UI, we do an HTTP post call with the required parameters.
        final HttpPost httpPost = new HttpPost("http://localhost:8080/api/login");
        final HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addTextBody("username","ashish.raghavan@google.com")
                .addTextBody("password","12345")
                .addTextBody("auth_flow","true")
                .addTextBody("redirect_uri","http://localhost:8080")
                .addTextBody("client_id","1095369")
                .addTextBody("device_uid","GOOGLECHROME")
                .addTextBody("scopes","seller")
                .build();
        httpPost.setEntity(httpEntity);
        final CloseableHttpResponse loginResponse = httpClient.execute(httpPost);
        Assert.assertNotNull(loginResponse);
        //We make sure that the server does an HttpStatus.SEE_OTHER redirection instead of a temporary re-direct.
        Assert.assertTrue(loginResponse.getStatusLine().getStatusCode() == HttpStatus.SEE_OTHER.value());
        //The value obtained from the location header should be URL for the consent.html.
        final Header consentUrlLocation = loginResponse.getFirstHeader("Location");
        Assert.assertNotNull(consentUrlLocation);
        //We parse out the authorization code from the location value to get the access token in the next step.
        final String consentUriValue = consentUrlLocation.getValue();
        Assert.assertNotNull(consentUriValue);
        //Make sure there is an authorization code value within the consent uri value.
        Assert.assertTrue(consentUriValue.contains("authorization_code"));
        int indexOfAuthorizationCode = consentUriValue.indexOf("authorization_code");
        Assert.assertTrue(indexOfAuthorizationCode != -1 && indexOfAuthorizationCode > 0);
        final String authorizationCodeKeyValue = consentUriValue.substring(indexOfAuthorizationCode,consentUriValue.indexOf("&",indexOfAuthorizationCode));
        Assert.assertNotNull(authorizationCodeKeyValue);
        final String[] authorizationCodeKVPair = authorizationCodeKeyValue.split("=");
        Assert.assertTrue(authorizationCodeKVPair.length == 2);
        //Get the value stored at index 1.
        final String authorizationCode = authorizationCodeKVPair[1];
        Assert.assertNotNull(authorizationCode);
        //Now request the token using this authorization code. We skip the consent part because this test is for
        //testing the part which functions after we give the consent.

        final HttpPost tokenPost = new HttpPost("http://localhost:8080/api/oauth/token");
        //Set the parameters.
        final HttpEntity tokenEntity = MultipartEntityBuilder.create().addTextBody("authorization_code",authorizationCode)
                .addTextBody("client_id","1095369")
                .build();
        tokenPost.setEntity(tokenEntity);
        final CloseableHttpResponse tokenResponse = httpClient.execute(tokenPost);
        Assert.assertTrue(tokenResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value());
        //Get the response string and assert it's not null.
        final String responseString = EntityUtils.toString(tokenResponse.getEntity());
        Assert.assertTrue(!Strings.isNullOrEmpty(responseString));
        final Map<String,Object> responseMap = mapper.readValue(responseString,typeMapStringObject);
        Assert.assertTrue(responseMap != null && !responseMap.isEmpty());
        Assert.assertNotNull(responseMap.get("user"));
        Assert.assertTrue(responseMap.get("user") instanceof Map);
        final Map<String,Object> userMap = (Map<String,Object>)responseMap.get("user");
        Assert.assertNotNull(userMap.get("email"));
        email = userMap.get("email").toString();
        Assert.assertNotNull(responseMap.get("access_token"));
        Assert.assertNotNull(responseMap.get("refresh_token"));
        accessToken = responseMap.get("access_token").toString();
    }

    @Test(dependsOnMethods = "testRedirectionToLoginPage")
    @Rollback(value = false)
    public void testAccessToken() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Assert.assertTrue(!Strings.isNullOrEmpty(accessToken));
        Assert.assertTrue(!Strings.isNullOrEmpty(email));
        final HttpGet httpGet = new HttpGet("http://localhost:8080/api/user/"+(email.replace("@","%40")));
        httpGet.addHeader("Authorization","Bearer "+accessToken);
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value());
        final String responseString = EntityUtils.toString(httpResponse.getEntity());
        Assert.assertNotNull(responseString);
        final Map<String,Object> responseMap = mapper.readValue(responseString,typeMapStringObject);
        Assert.assertNotNull(responseMap.get("email"));
    }
}
