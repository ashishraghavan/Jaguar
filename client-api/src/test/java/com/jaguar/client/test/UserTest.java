package com.jaguar.client.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jaguar.om.IAccount;
import com.jaguar.om.IDevice;
import com.jaguar.om.IUser;
import com.jaguar.om.IUserRole;
import com.jaguar.om.impl.Account;
import com.jaguar.om.impl.Device;
import com.jaguar.om.impl.User;
import com.jaguar.om.impl.UserRole;
import com.jaguar.om.test.BaseTestCase;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Test
public class UserTest extends BaseTestCase {

    private static final Logger userTestLogger = Logger.getLogger(UserTest.class.getSimpleName());
    private static final Set<String> queryKeys = ImmutableSet.of("email","code","device_uid","role");
    private final Map<String,String> formMap = ImmutableMap.<String,String>builder()
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

    private static final Properties properties = new Properties();
    private static final String GMAIL_HOST = "smtp.gmail.com";
    private static final String GMAIL_USERNAME = "jaguardevelopmental@gmail.com";
    private static final String GMAIL_PASSWORD = "Halos12345";
    static {
        properties.setProperty("mail.smtp.host","smtp.gmail.com");
        properties.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.smtp.port","465");
    }

    private final Comparator<Message> messageComparator = (message1, message2) -> {
        try {
            return Long.compare(message2.getSentDate().getTime(),
                    message1.getSentDate().getTime());
        } catch (Exception e) {
            userTestLogger.error("There was an error comparing message sent times with exception "+e.getLocalizedMessage());
        }
        return -1;
    };
    /**
         * {
             "creationDate" : null,
             "modificationDate" : null,
             "active" : false,
             "id" : 6,
             "name" : "AshishRaghavan",
             "firstName" : "Ashish",
             "lastName" : "Raghavan",
                 "account" : {
                     "creationDate" : 1508703087458,
                     "modificationDate" : 1508703087458,
                     "active" : true,
                     "id" : 1,
                     "accountName" : "Jaguar",
                     "city" : "Long Is City",
                     "country" : "USA",
                     "state" : "NY",
                     "postalCode" : "11101"
                 },
             "email" : "ashishraghavan13687@gmail.com",
             "lastOnline" : null,
             "phoneNumber" : "4082216275"
            }
     *
     */
    @Test
    @Rollback(value = false)
    @SuppressWarnings("unchecked")
    public void registerUser() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final MultipartEntityBuilder userEntityBuilder = MultipartEntityBuilder.create();
        for(String formKey : formMap.keySet()) {
             userEntityBuilder.addTextBody(formKey,formMap.get(formKey));
        }
        final HttpPost httpPost = new HttpPost("http://localhost:8080/client/api/user/");
        httpPost.setEntity(userEntityBuilder.build());
        final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        final String response = EntityUtils.toString(httpResponse.getEntity());
        final Map<String,Object> responseMap = mapper.readValue(response,typeMapStringObject);
        Assert.assertNotNull(responseMap.get("firstName"));
        Assert.assertTrue(responseMap.get("firstName").toString()
                .equals(formMap.get("first_name")));
        Assert.assertNotNull(responseMap.get("lastName"));
        Assert.assertTrue(responseMap.get("lastName").toString()
                .equals(formMap.get("last_name")));
        Assert.assertTrue(responseMap.get("email").equals(formMap.get("username")));
        Assert.assertNotNull(responseMap.get("account"));
        Assert.assertTrue(responseMap.get("account") instanceof Map);
        final Map<String,Object> accountMap = (Map<String,Object>)responseMap.get("account");
        Assert.assertNotNull(accountMap);
        Assert.assertNotNull(accountMap.get("accountName"));
        Assert.assertTrue(accountMap.get("accountName").equals("Jaguar"));
    }

    @Test(dependsOnMethods = "registerUser")
    @Rollback(value = false)
    public void verifyEmail() throws Exception {
        Session session = Session.getDefaultInstance(properties, null);
        Store store = session.getStore("imaps");
        store.connect(GMAIL_HOST, GMAIL_USERNAME, GMAIL_PASSWORD);
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        // creates a search criterion
        final SearchTerm searchCondition = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                //matching condition is if the message is UNREAD and the subject line is "Verify your registration" and
                //sender is postmaster@ashishraghavan.me
                try {
                    return !(message.isSet(Flags.Flag.SEEN)) &&
                            message.getSubject().equalsIgnoreCase("Verify your registration");
                } catch (Exception e) {
                    userTestLogger.error("Failed to process matching criteria with exception "+e.getLocalizedMessage());
                    return false;
                }
            }
        };
        // performs search through the folder
        Message[] foundMessages = inbox.search(searchCondition);
        Assert.assertNotNull(foundMessages);
        final int noOfTries = 3; //(3 * 3 minutes) //0 based counting
        if(foundMessages.length <= 0) {
            for(int i=0;i<noOfTries;i++) {
                TimeUnit.MINUTES.sleep(2);
                foundMessages = inbox.search(searchCondition);
                if(foundMessages.length > 0) {
                    break;
                }
            }
        }
        Assert.assertTrue(foundMessages.length > 0);
        final List<Message> messageList = Arrays.asList(foundMessages);
        //Do a sort only if there is more than one message that matches this criteria.
        if(messageList.size() > 1) {
            messageList.sort(messageComparator);
        }
        //This is the message we are interested in
        final Message message = messageList.get(0);
        Assert.assertNotNull(message);
        final Object objMessageContent = message.getContent();
        Assert.assertNotNull(objMessageContent);
        //Get the link from the message content.
        final String messageContent = (String)objMessageContent;
        //Parse the link out of this.
        Assert.assertNotNull(messageContent);
        final int indexOfScheme = messageContent.indexOf("http");
        //Mark this message as read.
        Assert.assertTrue(indexOfScheme != -1);
        //Remove any trailing \r or \n from the email message.
        final String link = messageContent.substring(indexOfScheme).trim()
                .replace("\r","").replace("\n","");
        //See if we have a correct URI.
        final URI uri = URI.create(link);
        Assert.assertTrue(!Strings.isNullOrEmpty(uri.getScheme()));
        Assert.assertTrue(!Strings.isNullOrEmpty(uri.getAuthority()));
        Assert.assertTrue(!Strings.isNullOrEmpty(uri.getQuery()));
        final String queryString = uri.getQuery();
        final String[] splitQuery = queryString.split("&");
        Assert.assertTrue(splitQuery.length > 0);
        for(String queryKeyValue : splitQuery) {
            final String[] queryKV = queryKeyValue.split("=");
            Assert.assertTrue(queryKV.length == 2);
            Assert.assertTrue(queryKeys.contains(queryKV[0]));
            if(queryKV[0].equals("email")) {
                final String emailValue = queryKV[1];
                Assert.assertTrue(emailValue.equals(formMap.get("username")));
            }
        }
        //Access the link
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet(uri);
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        final String strResponse = EntityUtils.toString(httpResponse.getEntity());
        final Map<String,Object> responseMap = mapper.readValue(strResponse,typeMapStringObject);
        Assert.assertNotNull(responseMap);
    }

    //Request an authorization code
    /**
     * http://localhost:8080/client/api/oauth/authorize?response_type=json&client_id=1095369&
     * redirect_uri=http://localhost:8080/client/api/files/redirection.html&scope=seller&device_uid=iOS6sPlus-A1687
     */
    @Test(dependsOnMethods="verifyEmail")
    @Rollback(value = false)
    public void requestAuthorization() throws Exception {
        //Turn off automatic redirection handling.
        final CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
        final HttpGet httpGet = new HttpGet("http://localhost:8080/client/api/oauth/authorize?" +
                "response_type=json&" +
                "client_id="+formMap.get("client_id") + "&" +
                "redirect_uri=http://localhost:8080/client/api/files/redirection.html&scope=seller&device_uid="+formMap.get("device_uid"));
        final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT);
        //Get the location header.
        final org.apache.http.Header locationHeader = httpResponse.getFirstHeader("Location");
        Assert.assertNotNull(locationHeader);
        final String location = locationHeader.getValue();
        Assert.assertNotNull(location);
        final URI loginUri = URI.create(location);
        EntityUtils.consume(httpResponse.getEntity());
        final HttpGet getLogin = new HttpGet(loginUri);
        final CloseableHttpResponse loginResponse = httpClient.execute(getLogin);
        Assert.assertTrue(loginResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        final org.apache.http.Header contentTypeHeader = loginResponse.getFirstHeader("Content-Type");
        Assert.assertNotNull(contentTypeHeader);

        //We must have the login page now.

    }

    @AfterTest(alwaysRun = true)
    @Transactional
    @Rollback(value = false)
    public void deleteUser() throws Exception {
        //delete from jaguar_device where user_id = (select user_id from jaguar_user where email = 'jaguardevelopmental@gmail.com');
        //Get the account first.
        IAccount account = new Account("Jaguar");
        account = getDao().loadSingleFiltered(account, null, false);
        Assert.assertNotNull(account);
        //Get the user now.
        IUser user = new User(account, formMap.get("username"));
        user = getDao().loadSingleFiltered(user, null, false);
        if(user == null) {
            return;
        }
        //Get the device and delete it.
        IDevice device = new Device(formMap.get("device_uid"), user);
        device = getDao().loadSingleFiltered(device,null,false);
        if(device == null) {
            //delete the user and return.
            getDao().remove(user);
            return;
        }
        getDao().remove(device);
        IUserRole userRole = new UserRole();
        userRole.setUser(user);
        final List<IUserRole> userRoles = getDao().loadFiltered(userRole,false);
        if(userRoles != null && userRoles.size() > 0) {
            for(IUserRole userRole1 : userRoles) {
                getDao().remove(userRole1);
            }
        }
        //Now delete the user.
        getDao().remove(user);
    }
}
