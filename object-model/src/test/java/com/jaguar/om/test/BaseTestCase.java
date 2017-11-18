package com.jaguar.om.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.jaguar.om.IBaseDAO;
import com.jaguar.om.ICategoryDAO;
import com.jaguar.om.IDeviceApplication;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;

import java.net.URI;
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
    private ICategoryDAO categoryDAO;

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
    }

    @Autowired
    @Qualifier(value = "categoryDao")
    public void setCategoryDAO(ICategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }
    ICategoryDAO getCategoryDAO() {
        return this.categoryDAO;
    }
    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    protected static final MapType typeMapStringObject
            = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
    @SuppressWarnings("unused")
    protected static final MapType typeMapStringString
            = typeFactory.constructMapType(HashMap.class, String.class, String.class);
    @SuppressWarnings("unused")
    protected static CollectionType typeListMap = typeFactory.constructCollectionType(ArrayList.class,
            typeFactory.constructType(HashMap.class));
    private String accessToken;

    protected final Map<String,String> userRegistrationMap = ImmutableMap.<String,String>builder()
            .put("username","jaguardevelopmental@gmail.com")
            .put("password","12345")
            .put("device_uid","ZX1G3234RGT")
            .put("model","Nexus6P")
            .put("first_name","Ashish")
            .put("last_name","Raghavan")
            .put("phone","4082216275")
            .put("client_id","1095369")
            .put("api","21")
            .put("role","seller")
            .build();
    private final String redirectionUri = "http://localhost:18080/client/api/files/redirection.html&scope=seller&device_uid="+ userRegistrationMap.get("device_uid");
    private final Map<String,String> userLoginMap = ImmutableMap.<String,String>builder()
            .put("username",userRegistrationMap.get("username"))
            .put("password",userRegistrationMap.get("password"))
            .put("device_uid",userRegistrationMap.get("device_uid"))
            .put("auth_flow","true")
            .put("redirect_uri",redirectionUri)
            .put("client_id",userRegistrationMap.get("client_id"))
            .put("scopes",userRegistrationMap.get("role"))
            .put("api",userRegistrationMap.get("api"))
            .put("model",userRegistrationMap.get("model"))
            .build();

    protected void requestAuthorizationAndGetToken() throws Exception {
        //Turn off automatic redirection handling.
        final CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
        final HttpGet httpGet = new HttpGet("http://localhost:"+CLIENT_PORT+"/client/api/oauth/authorize?" +
                "response_type=json&" +
                "client_id="+ userRegistrationMap.get("client_id") + "&" +
                "redirect_uri="+redirectionUri);
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        final String strAuthorizationRequestResponse = EntityUtils.toString(httpResponse.getEntity());
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_TEMPORARY_REDIRECT,"Failed with message "+strAuthorizationRequestResponse);
        //Get the location header.
        final org.apache.http.Header locationHeader = httpResponse.getFirstHeader("Location");
        Assert.assertNotNull(locationHeader);
        final String location = locationHeader.getValue();
        Assert.assertNotNull(location);
        final URI loginUri = URI.create(location);
        final HttpGet getLogin = new HttpGet(loginUri);
        final CloseableHttpResponse loginResponse = httpClient.execute(getLogin);
        Assert.assertTrue(loginResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK,"Failed with message "+loginResponse.getStatusLine());
        final org.apache.http.Header contentTypeHeader = loginResponse.getFirstHeader("Content-Type");
        Assert.assertNotNull(contentTypeHeader);
        final String contentTypeValue = contentTypeHeader.getValue();
        Assert.assertNotNull(contentTypeValue);
        Assert.assertTrue(contentTypeValue.equals("text/html"));
        //We must have the login page now.
        final MultipartEntityBuilder loginFormEntityBuilder = MultipartEntityBuilder.create();
        for(String formKey : userLoginMap.keySet()) {
            loginFormEntityBuilder.addTextBody(formKey, userLoginMap.get(formKey));
        }
        //We need to go against the login endpoint at http://localhost:8080/client/api/login
        final HttpPost httpPost = new HttpPost("http://localhost:"+CLIENT_PORT+"/client/api/login");
        httpPost.setEntity(loginFormEntityBuilder.build());
        final CloseableHttpResponse afterLoginResponse = httpClient.execute(httpPost);
        final String afterLoginResponseStr = EntityUtils.toString(afterLoginResponse.getEntity());
        Assert.assertTrue(afterLoginResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_SEE_OTHER,"Failed with message "+afterLoginResponseStr);
        final org.apache.http.Header afterLoginHeader = afterLoginResponse.getFirstHeader("Location");
        Assert.assertNotNull(afterLoginHeader);
        final String afterLoginHeaderValue = afterLoginHeader.getValue();
        Assert.assertNotNull(afterLoginHeaderValue);
        final URI consentUri = URI.create(afterLoginHeaderValue);
        //Get all the query parameters.
        final String consentQueryStr = consentUri.getQuery();
        final String[] splitConsentStr = consentQueryStr.split("&");
        Assert.assertTrue(splitConsentStr.length > 0);
        final HttpGet getConsentRequest = new HttpGet(consentUri);
        final CloseableHttpResponse beforeConsentResponse = httpClient.execute(getConsentRequest);
        final String beforeConsentResponseStr = EntityUtils.toString(beforeConsentResponse.getEntity());
        //Get the consent page.
        Assert.assertTrue(beforeConsentResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK,"Failed with message "+beforeConsentResponseStr);
        Assert.assertNotNull(beforeConsentResponse.getFirstHeader("Content-Type"));
        Assert.assertNotNull(beforeConsentResponse.getFirstHeader("Content-Type").getValue());
        Assert.assertTrue(beforeConsentResponse.getFirstHeader("Content-Type").getValue().equals("text/html"));
        //Update the consent.
        final HttpPost updateConsentPost = new HttpPost("http://localhost:"+CLIENT_PORT+"/client/api/oauth/update");
        final MultipartEntityBuilder consentEntityBuilder = MultipartEntityBuilder.create();
        for(String consentKeyValue : splitConsentStr) {
            final String[] splitConsentKV = consentKeyValue.split("=");
            consentEntityBuilder.addTextBody(splitConsentKV[0],splitConsentKV[1]);
        }
        //Set the authorization to AGREE.
        consentEntityBuilder.addTextBody("authorization",String.valueOf(IDeviceApplication.Authorization.AGREE));
        updateConsentPost.setEntity(consentEntityBuilder.build());
        final CloseableHttpResponse authorizationResponse = httpClient.execute(updateConsentPost);
        final String authorizationResponseStr = EntityUtils.toString(authorizationResponse.getEntity());
        Assert.assertTrue(authorizationResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_SEE_OTHER,"Failed with message "+authorizationResponseStr);
        final org.apache.http.Header redirectionHeader = authorizationResponse.getFirstHeader("Location");
        Assert.assertNotNull(redirectionHeader);
        final String redirectionHeaderValue = redirectionHeader.getValue();
        Assert.assertNotNull(redirectionHeaderValue);
        //No need to access this location. We just need the authorization code from the link.
        final URI redirectionURI = URI.create(redirectionHeaderValue);
        final String redirectionQuery = redirectionURI.getQuery();
        Assert.assertNotNull(redirectionQuery);
        String authorizationCode = null;
        final String[] splitRedirectionQuery = redirectionQuery.split("&");
        Assert.assertTrue(splitRedirectionQuery.length > 0);
        for(String redirectionQueryKeyValue : splitRedirectionQuery) {
            final String[] redirectionQueryKV = redirectionQueryKeyValue.split("=");
            Assert.assertNotNull(redirectionQueryKV[0]);
            if(redirectionQueryKV[0].equals("authorization_code")) {
                authorizationCode = redirectionQueryKV[1];
            }
        }
        Assert.assertNotNull(authorizationCode);
        //Finally get the token using this code.
        final HttpPost tokenPost = new HttpPost("http://localhost:"+CLIENT_PORT+"/client/api/oauth/token");
        final MultipartEntityBuilder tokenBuilder = MultipartEntityBuilder.create();
        tokenBuilder.addTextBody("authorization_code",authorizationCode);
        tokenBuilder.addTextBody("client_id",userRegistrationMap.get("client_id"));
        tokenBuilder.addTextBody("device_uid",userRegistrationMap.get("device_uid"));
        tokenPost.setEntity(tokenBuilder.build());
        final CloseableHttpResponse tokenResponse = httpClient.execute(tokenPost);
        final String stringifiedTokenResponse = EntityUtils.toString(tokenResponse.getEntity());
        Assert.assertTrue(tokenResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK,"Failed with message "+stringifiedTokenResponse);
        final Map<String,Object> tokenMap = mapper.readValue(stringifiedTokenResponse,typeMapStringObject);
        Assert.assertNotNull(tokenMap.get("access_token"));
        Assert.assertNotNull(tokenMap.get("refresh_token"));
        accessToken = (String)tokenMap.get("access_token");
    }

    protected String getAccessToken() {
        return accessToken;
    }
}
