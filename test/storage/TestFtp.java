
package storage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpClientProvider;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpProtocolException;
import vellum.jx.JConsoleMap;
import vellum.jx.JMaps;
import vellum.util.Args;
import vellum.util.Lists;
import vellum.util.MimeTypes;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class TestFtp {
    Logger logger = LoggerFactory.getLogger(TestFtp.class);
    String storageDir = "/pri/angulardemo/storage";
    String configFile = "/pri/angulardemo/test/ftp.json";
    JConsoleMap properties;
    FtpClient ftpClient;
    String id;
    String hostname;
    String username;
    char[] password;
    String deleteDir;
    long connectTimeout;
    long readTimeout;
    int port = 21;
    boolean enabled;
    
    public TestFtp() throws Exception {
        logger.info("config {} {}", configFile, Streams.readString(new File(configFile)));
        properties = JMaps.nullConsoleFile(configFile);
        enabled = properties.getBoolean("enabled", true);
        port = properties.getInt("port", port);
        id = properties.getString("id");
        hostname = properties.getString("hostname");
        username = properties.getString("username");
        password = properties.getPassword("password");
        storageDir = properties.getString("storageDir");
        deleteDir = properties.getString("deleteDir");
        connectTimeout = properties.getMillis("connectTimeout");
        readTimeout = properties.getMillis("readTimeout");
    }
    
    @BeforeClass
    public static void setUpClass() {
        BasicConfigurator.configure();
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
    public void deleteDir() throws Exception {
        login();
        deleteDirRecursively(deleteDir);
        ftpClient.close();
    }    
    
    private void login() throws FtpProtocolException, IOException {
        ftpClient = FtpClientProvider.provider().createFtpClient();
        ftpClient.setConnectTimeout((int) connectTimeout);
        ftpClient.setReadTimeout((int) readTimeout);
        ftpClient.connect(new InetSocketAddress(hostname, port));
        ftpClient.login(username, password);        
    }

    private boolean deleteDirRecursively(String path) {
        logger.info("deleteDirRecursively {} {}", id, path);
        try {
            for (FtpDirEntry entry : Lists.list(ftpClient.listFiles(path))) {
                if (entry.getName().charAt(0) == '.') {
                    continue;
                }
                logger.info("entry {}", entry.toString());
                if (isResource(entry.getName())) {
                    deleteFile(path + "/" + entry.getName());
                    logger.info("resource {}", Args.format(entry.getName(), entry.getLastModified()));
                } else {
                    logger.info("dir {}", Args.format(entry.getName(), entry.getLastModified()));
                    deleteDirRecursively(path + "/" + entry.getName());
                }
            }
            if (isResource(path)) {
                deleteFile(path);
            } else {
                deleteDir(path);
            }
            return true;
        } catch (FtpProtocolException | IOException e) {
            logger.warn("deleteDirRecursively {} {}", path, e.getMessage());
            return false;
        }
    }

    private boolean isResource(String path) {
        return MimeTypes.getContentType(path, null) != null;
    }
    
    private boolean deleteDir(String path) {
        try {
            ftpClient.removeDirectory(path);
            logger.info("deleteDir {}", path);
            return true;
        } catch (RuntimeException e) {
            logger.warn("deleteDir {}", e.getMessage());
            return false;
        } catch (FtpProtocolException | IOException e) {
            logger.warn("deleteDir {}", e.getMessage());
            return false;
        }
    }
    
    private boolean deleteFile(String path) {
        try {
            ftpClient.deleteFile(path);
            logger.info("deleteFile {}", path);
            return true;
        } catch (RuntimeException e) {
            logger.warn("deleteFile {}", e.getMessage());
            return false;
        } catch (FtpProtocolException | IOException e) {
            logger.warn("deleteFile {}", e.getMessage());
            return false;
        }
    }
}
