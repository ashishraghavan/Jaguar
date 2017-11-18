package com.jaguar.client.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.jaguar.om.BuyingFormat;
import com.jaguar.om.test.BaseTestCase;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.test.annotation.Rollback;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_ACCEPTED;


/**
 * Integration tests for ProductService.
 */
//
@Test(groups = "product_test",dependsOnGroups = "client-user")
public class ProductIT extends BaseTestCase {

    private final String productUrl = "http://localhost:"+CLIENT_PORT+"/client/api/product";
    private String itemNumber;
    private final Map<String,String> productMap = ImmutableMap.<String,String>builder()
            .put("title","iPad Pro 32 GB Gold with Apple Pencil")
            .put("description","For sale is an iPad Pro Gold in excellent condition with an Apple Pencil")
            .put("price","450")
            .put("buying_format", String.valueOf(BuyingFormat.AUCTION))
            .put("category","Tablets")
            .put("currency_name","USD")
            .build();

    @Test
    @Rollback(value = false)
    public void testGetAccessToken() throws Exception {
        requestAuthorizationAndGetToken();
    }

    @Test(dependsOnMethods = "testGetAccessToken")
    @Transactional
    @Rollback(value = false)
    public void testCreateProduct() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(productUrl);
        httpPost.addHeader("Authorization","Bearer "+getAccessToken());
        final MultipartEntityBuilder productBuilder = MultipartEntityBuilder.create();
        for(String productProperty : productMap.keySet()) {
            productBuilder.addTextBody(productProperty,productMap.get(productProperty));
        }
        httpPost.setEntity(productBuilder.build());
        final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        final String httpResponseStr = EntityUtils.toString(httpResponse.getEntity());
        Assert.assertTrue(httpResponse.getStatusLine().getStatusCode() == SC_ACCEPTED,"Failed with message "+httpResponseStr);
        final Map<String,Object> productResponseMap = mapper.readValue(httpResponseStr,typeMapStringObject);
        Assert.assertNotNull(productResponseMap);
        Assert.assertTrue(!productResponseMap.isEmpty());
        Assert.assertNotNull(productResponseMap.get("itemNumber"));
        itemNumber = (String)productResponseMap.get("itemNumber");
    }

    @Test(dependsOnMethods = "testCreateProduct")
    @Rollback(value = false)
    public void testCreateImages() throws Exception {
        final URL productImagesURL = Thread.currentThread().getContextClassLoader().getResource("product_images");
        Assert.assertNotNull(productImagesURL);
        final File productImagesDirectory = new File(productImagesURL.getPath());
        Assert.assertTrue(productImagesDirectory.exists());
        Assert.assertTrue(productImagesDirectory.isDirectory());
        final File[] fileList = productImagesDirectory.listFiles();
        Assert.assertNotNull(fileList);
        Assert.assertTrue(fileList.length > 0 );
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        MultipartEntityBuilder imageEntityBuilder = MultipartEntityBuilder.create();
        final HttpPost httpPost = new HttpPost(productUrl + "/" +itemNumber);
        httpPost.setHeader("Authorization","Bearer "+getAccessToken());
        for(File imageFile : fileList) {
            imageEntityBuilder.addBinaryBody("image", ByteStreams.toByteArray(new FileInputStream(imageFile)));
            imageEntityBuilder.addTextBody("file_name",imageFile.getName());
            httpPost.setEntity(imageEntityBuilder.build());
            final CloseableHttpResponse imageHttpResponse = httpClient.execute(httpPost);
            final String httpResponseStr = EntityUtils.toString(imageHttpResponse.getEntity());
            Assert.assertTrue(imageHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK,"Failed with message "
                    +httpResponseStr);
        }
    }
}
