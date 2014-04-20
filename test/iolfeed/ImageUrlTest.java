/*
 */

package iolfeed;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.Assert;

/**
 *
 * @author evanx
 */
public class ImageUrlTest {

    public ImageUrlTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void fetchImage() throws Exception {
        String imageUrl = "http://www.iol.co.za/polopoly_fs/copy-of-copy-of-si-nkandla-1.1668236!/image/1079177060.jpg_gen/derivatives/box_300/1079177060.jpg";
        int imageLength = 15556;
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.connect();
        int length = connection.getContentLength();
        Assert.assertEquals(imageLength, length);
        byte[] content = readBytes(new BufferedInputStream(connection.getInputStream()));
        Assert.assertEquals(imageLength, content.length);        
    }        
    
    public static byte[] readBytes(InputStream stream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while (true) {
                int b = stream.read();
                if (b < 0) {
                    return outputStream.toByteArray();
                }
                outputStream.write(b);
            }
        }
    }
    
}
