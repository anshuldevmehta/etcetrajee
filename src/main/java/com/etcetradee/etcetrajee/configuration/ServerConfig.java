package com.etcetradee.etcetrajee.configuration;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import java.util.List;

@Configuration
//@Lazy
public class ServerConfig {

    @Autowired
    private SSLContext sslContextAuto;

    @Value("${server.port}")
    private int serverPort;

    @Value("${etcd.url}")
    private String etcdUrl;


   // @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> {
            Ssl ssl = new Ssl();
            try {
                ssl.setEnabled(true); // Example path to a local keystore
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ssl.setKeyStoreType("PKCS12");
            ssl.setKeyAlias("myalias");
            ssl.setKeyPassword("123456");
            factory.setSsl(ssl);
            factory.setPort(serverPort);
        };
    }
//    @Configuration
//    public class SslConfig implements WebServerFactoryCustomizer<ServletWebServerFactory> {
//
//        @Override
//        public void customize(ServletWebServerFactory servletContainer) {
//            try {
//                servletContainer.setSslContext(sslContext);
//            } catch (Exception e) {
//                throw new IllegalStateException("Failed to initialize SSL context", e);
//            }
//        }
        @Bean
        public ServletWebServerFactory servletContainer() throws Exception {
            TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

            // Configure SSL
            tomcat.addAdditionalTomcatConnectors(redirectConnectorConfigs(tomcat.getAdditionalTomcatConnectors()));
            tomcat.addAdditionalTomcatConnectors(sslConnectorConfigs(tomcat.getAdditionalTomcatConnectors()));;
            return tomcat;
        }

        private Connector sslConnectorConfigs(List<Connector> connectors) throws Exception {
            SSLContext sslContext =sslContextAuto; // Initialize your SSLContext as shown earlier
            for (int i = 0; i < connectors.size(); i++) {
                if ("https".equals(connectors.get(i).getScheme())) {
                    connectors.get(i).setSecure(true);
                    connectors.get(i).setScheme("https");
                    break;
                }
            }
            return connectors.stream().findFirst().get();
        }

        private Connector redirectConnectorConfigs(List<Connector> connectors) {
            for (int i = 0; i < connectors.size(); i++) {
                if ("http".equals(connectors.get(i).getScheme()) &&!connectors.get(i).getScheme().equalsIgnoreCase("http")) {
                    connectors.get(i).setScheme("https");
                    break;
                }
            }
            return connectors.stream().findFirst().get();
        }

    private SSLContext loadEtcdProperties(KV kvClient) throws Exception {
        String keyStoreContentKey = "server.ssl.key-store-content";
        String keyStorePasswordKey = "server.ssl.key-store-password";
        String keyStoreTypeKey = "server.ssl.keyStoreType";
        String sslEnabledKey = "server.ssl.enabled";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        // Create KeyManagerFactory and initialize it with the keystore and password
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        // Load key store content and save to local file
        ByteSequence keyStoreContentSeq = ByteSequence.from(keyStoreContentKey.getBytes());
        String keyStoreContentBase64 = kvClient.get(keyStoreContentSeq).get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
        String keyStoreContentBaseless = kvClient.get(keyStoreContentSeq).get().getKvs().get(0).getValue().toString();
        byte[] keyStoreContent = Base64.getDecoder().decode(keyStoreContentBase64);
        // Files.write(Paths.get("local-springboothttps.p12"), keyStoreContent);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(keyStoreContent)) {
            keyStore.load(inputStream, "123456".toCharArray());
        }


        keyManagerFactory.init(keyStore, "123456".toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLContext.setDefault(sslContext);

        return sslContext;
    }
}
