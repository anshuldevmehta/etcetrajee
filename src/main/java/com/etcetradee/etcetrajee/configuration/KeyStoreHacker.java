package com.etcetradee.etcetrajee.configuration;

import com.etcetradee.etcetrajee.service.EtcdService;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Configuration
public class KeyStoreHacker {
    //@Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint httpConstraint = new SecurityConstraint();
                //  httpConstraint.addAuthenticationConstraint(new SecurityConstraint.AuthenticationConstraint(
                //        SecurityConstraint.ENFORCEMENT_LEVEL_REQUIRED,
                //      "CONFIDENTIAL"));
                //httpConstraint.
                //context.addConstraint(httpConstraint);
            }
        };
        return tomcat;
    }

    //@Bean
    public SSLContext getKeyStoreFromEtcd() throws Exception {

        String etcdEndpoint = "http://localhost:2379"; // Change to your etcd endpoint
        Client client = Client.builder().endpoints(etcdEndpoint).build();

        try (KV kvClient = client.getKVClient()) {
            ByteSequence keyBytes = ByteSequence.from("certyboy".getBytes());
            CompletableFuture<GetResponse> future = kvClient.get(keyBytes);
            GetResponse response = future.get(); // This blocks until the result is available
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            byte[] p12certbytes = response.getKvs().stream().findFirst().get().getValue().getBytes();
            keyStore.load(new ByteArrayInputStream(p12certbytes), "123456".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "123456".toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext;

        }
    }
}
