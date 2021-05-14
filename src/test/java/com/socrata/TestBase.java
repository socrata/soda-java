package com.socrata;

import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Consumer;
import com.socrata.api.Soda2Producer;
import com.socrata.api.SodaImporter;
import junit.framework.TestCase;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A Base class that pulls the config information for running the "unit" tests
 */
public class TestBase
{
    public static final String URL_PROP = "baseurl";
    public static final String USER_NAME_PROP = "username";
    public static final String PASSWORD_PROP = "password";
    public static final String API_KEY_PROP = "token";

    public static final String NOMINATION_DATA_SET = "nominationsCopy";
    public static final String UPDATE_DATA_SET = "testupdate";
    private static Logger log = Logger.getLogger("TestBase");



    static {
        // Create a trust manager that does not validate certificate chains.  This is only
        //needed when running tests against a non-official SODA2 instance.
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            public X509Certificate[] getAcceptedIssuers(){return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            ;
        }
    }

    /**
     * Creates an HttpLowLevel connection based on the properties in TestConfig.properties.
     *
     * @return  connection to use
     * @throws IOException
     */
    protected HttpLowLevel connect() throws IOException
    {
        Map<String, String> env = System.getenv();
        String url = env.getOrDefault("URL", null);
        return connect(url);
    }

    /**
     * Creates an HttpLowLevel connection based on the properties in TestConfig.properties.
     *
     * @return  connection to use
     * @throws IOException
     */
    protected HttpLowLevel connect(final String url) throws IOException
    {
        final Properties testProperties = new Properties();
        testProperties.load(getClass().getClassLoader().getResourceAsStream("TestConfig.properties"));
        Map<String, String> env = System.getenv();
        String username = env.getOrDefault("USERNAME", null);
        String password = env.getOrDefault("PASSWORD", null);
        String token = env.getOrDefault("TOKEN", null);
        log.info("HIIIIIIII");
        log.info(username);
        log.info(token);
        log.info(url);

        final HttpLowLevel httpLowLevel = HttpLowLevel.instantiateBasic(url == null ? testProperties.getProperty(URL_PROP) : url,
                                                                        username == null ? testProperties.getProperty(USER_NAME_PROP) : username,
                                                                        password == null ? testProperties.getProperty(PASSWORD_PROP) : password,
                                                                        token == null ? testProperties.getProperty(API_KEY_PROP) : token,
                                                                        null);
        return httpLowLevel;
    }

    protected Soda2Consumer createConsumer() throws IOException {
        return new Soda2Consumer(connect());
    }

    protected Soda2Producer createProducer() throws IOException {
        return new Soda2Producer(connect());
    }

    protected SodaImporter createImporter() throws IOException {
        return new SodaImporter(connect());
    }
}
