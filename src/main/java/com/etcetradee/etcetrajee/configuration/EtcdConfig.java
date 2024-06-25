package com.etcetradee.etcetrajee.configuration;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Configuration
public class EtcdConfig {

    @Value("${etcd.url}")
    private String etcdUrl;

    @Bean
    public Client etcdClient() {
        return Client.builder().endpoints(etcdUrl).build();
    }

    @Bean
    public KV kvClient(Client etcdClient) {
        return etcdClient.getKVClient();
    }

    @Bean
    @Primary
    public SSLContext loadEtcdProperties(KV kvClient) throws Exception {
        String keyStoreContentKey = "server.ssl.key-store-content";
        String keyStorePasswordKey = "server.ssl.key-store-password";
        String keyStoreTypeKey = "server.ssl.keyStoreType";
        String sslEnabledKey = "server.ssl.enabled";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        // Create KeyManagerFactory and initialize it with the keystore and password
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        //keyManagerFactory.init(keyStore, keyStorePasswordKey.toCharArray());

        // Load key store content and save to local file
        ByteSequence keyStoreContentSeq = ByteSequence.from(keyStoreContentKey.getBytes());
        String keyStoreContentBase64 = kvClient.get(keyStoreContentSeq).get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
        String keyStoreContentBaseless = kvClient.get(keyStoreContentSeq).get().getKvs().get(0).getValue().toString();
        byte[] keyStoreContent = Base64.getDecoder().decode(keyStoreContentBase64);
       // Files.write(Paths.get("local-springboothttps.p12"), keyStoreContent);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(keyStoreContent)) {
            keyStore.load(inputStream, "123456".toCharArray());
        }



        // Load other SSL properties
//        String keyStorePassword = kvClient.get(ByteSequence.from(keyStorePasswordKey, StandardCharsets.UTF_8)).get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
//        String keyStoreType = kvClient.get(ByteSequence.from(keyStoreTypeKey, StandardCharsets.UTF_8)).get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
//        String sslEnabled = kvClient.get(ByteSequence.from(sslEnabledKey, StandardCharsets.UTF_8)).get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
//        keyStore.load(null, null); // Initialize an empty keystore
//        keyStore.setCertificateEntry("etcd_certificate", keyStoreContentBaseless);
//        // Create TrustManagerFactory
//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
//                TrustManagerFactory.getDefaultAlgorithm());
        //trustManagerFactory.init(keyStore);
        // Set system properties for SSL
//        System.setProperty("server.ssl.key-store", "local-springboothttps.p12");
//        System.setProperty("server.ssl.key-store-password", keyStorePassword);
//        System.setProperty("server.ssl.keyStoreType", keyStoreType);
//        System.setProperty("server.ssl.enabled", sslEnabled);

            //ByteSequence keyBytes = ByteSequence.from("certyboy".getBytes());
//            CompletableFuture<GetResponse> future = kvClient.get(keyStoreContentSeq);
//            GetResponse response = future.get(); // This blocks until the result is available
            //KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            //byte[] p12certbytes = response.getKvs().stream().findFirst().get().getValue().getBytes();
            //keyStore.load(new ByteArrayInputStream(keyStoreContentBaseless.getBytes()), keyStorePassword.toCharArray());
//
            //KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "123456".toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLContext.setDefault(sslContext);

        //SSLContext sslContext = SSLContext.getInstance("TLS");
       // sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
            //return sslContext;

    }

    // Helper method to parse the certificate string
//    private PKCS12 parseCertificate(String certificate) throws CertificateException, IOException {
//        CertificateFactory certFactory = CertificateFactory.getInstance("PKCS12");
//        try (InputStream in = new ByteArrayInputStream(certificate.getBytes())) {
//            return (PKCS12) certFactory.generateCertificate(in);
//        }
//    }
}