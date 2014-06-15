
package storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.ssl.OpenHostnameVerifier;
import vellum.ssl.OpenTrustManager;
import vellum.ssl.SSLContexts;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class PostgraIntegrationTest {
    Logger logger = LoggerFactory.getLogger(PostgraIntegrationTest.class);
    String storageDir = "/pri/angulardemo/storage";
    int connectTimeout = 30000;
    int readTimeout = 30000;    
    SSLContext sslContext;
        
    public PostgraIntegrationTest() {
    }
    
    @Test 
    public void test() throws Exception {
        String path = "top/articles.json";
        File file = new File(storageDir, path);
        Assert.assertTrue(file.exists());
        byte[] bytes = Streams.readBytes(file);
        String json = new String(bytes);
        logger.info("json: {}", json);
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray array = jsonObject.get("articles").getAsJsonArray();
        Assert.assertTrue(array.size() >= 5);
        sslContext = SSLContexts.create(new OpenTrustManager());
        String url = "https://localhost:8443/api/content/" + path;
        post(url, bytes);
    }    
   
    public String post(String urlString, byte[] bytes) throws IOException {
        logger.trace("post {} {}", urlString, bytes.length);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(urlString).openConnection();
        try {
            connection.setHostnameVerifier(new OpenHostnameVerifier());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(bytes);
            }
            logger.info("responseCode {}", connection.getResponseCode());
            String response;
            try (InputStream inputStream = connection.getInputStream()) {
                response = Streams.readString(inputStream);
            }
            logger.info("response {}", response);
            return response.trim();
        } finally {
            connection.disconnect();
        }
    }    
    
    @BeforeClass
    public static void setUpClass() {
        BasicConfigurator.configure();
    }    
}
