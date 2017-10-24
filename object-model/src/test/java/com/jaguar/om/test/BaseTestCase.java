package com.jaguar.om.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jaguar.om.IBaseDAO;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ContextConfiguration(locations = {"classpath:spring-config.xml"})
public abstract class BaseTestCase extends AbstractTransactionalTestNGSpringContextTests {
    protected static final String CLIENT_PORT = "18080";
    final String[] COMMON_EXCLUDE_PROPERTIES = new String[]{"creationDate","modificationDate","active"};
    protected final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    private IBaseDAO dao;

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }
    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    protected static final MapType typeMapStringObject
            = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
    protected static final MapType typeMapStringString
            = typeFactory.constructMapType(HashMap.class, String.class, String.class);
    protected static CollectionType typeListMap = typeFactory.constructCollectionType(ArrayList.class,
            typeFactory.constructType(HashMap.class));
    private String email;
    private String accessToken;

    /**
     * Utility method to get the authorization for given user form details (mostly used after a test registration).
     * @throws Exception If an error occurred during this test.
     * 1095369
     * http://localhost:8080
     * seller
     * GOOGLECHROME
     *
     *
     * authflow - true
     */
    @SuppressWarnings("unchecked")
    protected void doAuthorizationAndAuthentication(final String userName,
                                                    final String password,
                                                    final String authFlow,
                                                    final String redirectUri,
                                                    final String client_id,
                                                    final String device_uid,
                                                    final String scopes) throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAuthCaching().disableRedirectHandling().build();
        final HttpGet httpGet = new HttpGet("http://localhost:8080/api/oauth/authorize?response_type=json&client_id="+client_id +
                "&redirect_uri="+redirectUri+"&scope="+scopes+"&device_uid="+device_uid);
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.TEMPORARY_REDIRECT.value());
        final Header locationHeader = httpResponse.getFirstHeader("Location");
        Assert.assertNotNull(locationHeader);
        //Assertion that the location value from the header is not null.
        Assert.assertNotNull(locationHeader.getValue());
        //Since we don't have the UI, we do an HTTP post call with the required parameters.
        final HttpPost httpPost = new HttpPost("http://localhost:8080/api/login");
        final HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addTextBody("username",userName)
                .addTextBody("password",password)
                .addTextBody("auth_flow",authFlow)
                .addTextBody("redirect_uri",redirectUri)
                .addTextBody("client_id",client_id)
                .addTextBody("device_uid",device_uid)
                .addTextBody("scopes",scopes)
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

    protected String getEmail() {
        return email;
    }

    protected String getAccessToken() {
        return accessToken;
    }
}
