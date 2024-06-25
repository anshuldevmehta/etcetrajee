package com.etcetradee.etcetrajee.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;


@Service
public class EtcdService {

    private final Client client;

    public EtcdService() {
        String etcdEndpoint = "http://localhost:2379"; // Change to your etcd endpoint
        this.client = Client.builder().endpoints(etcdEndpoint).build();
    }

    /**
     * 1. Creates a new Keystore
     * 2. Converts a new
     * @param key
     * @return
     */
    public CompletableFuture<PutResponse> loadKeyStoreInEtcd(String key) {
        try (var kvClient = client.getKVClient()) {
            // Load the KeyStore from a file
            FileInputStream fis = new FileInputStream(new File("C:\\IntellijProjects\\etcetrajee\\src\\main\\resources\\springboothttps.p12"));
           // KeyStore keyStore = KeyStore.getInstance("PKCS12");
          //  keyStore.load(new FileInputStream(new File("C:\\IntellijProjects\\etcetrajee\\src\\main\\resources\\springboothttps.p12")), "123456".toCharArray());
            // Convert the KeyStore to a ByteSequence
           // byte[] keystoreBytes = KeyStoreConverter.toByteArray(keyStore);
            ByteSequence keyStoreByteSequence = ByteSequence.from(Base64.getEncoder().encode(fis.readAllBytes()));
            //keyStore.load(fis, "123456".toCharArray());

            ByteSequence keyBytes = ByteSequence.from(key.getBytes(StandardCharsets.UTF_8));

            return kvClient.put(keyBytes, keyStoreByteSequence);
        } catch (Exception e) {
            throw new RuntimeException("Failed to put value in etcd", e);
        }
    }
    public CompletableFuture<PutResponse> putValueInEtcd(String key, String value) {
        try (var kvClient = client.getKVClient()) {
            ByteSequence keyBytes = ByteSequence.from(key.getBytes(StandardCharsets.UTF_8));
            ByteSequence valueBytes = ByteSequence.from(value.getBytes(StandardCharsets.UTF_8));

            return kvClient.put(keyBytes, valueBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to put value in etcd", e);
        }
    }

    public void getKeyStoreFromEtcd(String key) {
        try (KV kvClient = client.getKVClient()) {
            ByteSequence keyBytes = ByteSequence.from(key.getBytes());
            CompletableFuture<GetResponse> future = kvClient.get(keyBytes);
            GetResponse response = future.get(); // This blocks until the result is available
            //ByteSequence p12certbits = response.getKvs().stream().findFirst().get().getValue();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
           // keyStore.load(new FileInputStream(new File("C:\\IntellijProjects\\etcetrajee\\src\\main\\resources\\springboothttps.p12")), "123456".toCharArray());
           // byte[] p12certbytes=response.getKvs().stream().findFirst().get().getValue().getBytes();
            //String utf8String = new String(p12certbytes, StandardCharsets.UTF_8);
               // byte[] p12certbytes = Base64.getMimeDecoder().decode(new String(response.getKvs().stream().findFirst().get().getValue().getBytes(), StandardCharsets.UTF_8));
                byte[] p12certbytes = response.getKvs().stream().findFirst().get().getValue().getBytes();
               // ByteSequence p12certbits = ByteSequence.from(p12certbytes);
                keyStore.load(new ByteArrayInputStream(p12certbytes), "123456".toCharArray());


            String keyStorePassword = "123456";
            String keyAlias = "myalias";

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            //SSLContext sslContext = SSLContext.getInstance("TLS");
            //sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            //return keyStore;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get value from etcd", e);
        }

    }
    public String getValueFromEtcd(String key) {
        try (KV kvClient = client.getKVClient()) {
            ByteSequence keyBytes = ByteSequence.from(key.getBytes());
            CompletableFuture<GetResponse> future = kvClient.get(keyBytes);
            GetResponse response = future.get(); // This blocks until the result is available
            if(response.getKvs().isEmpty())
                return "Aisa Kuch Nahi Mila";
            return response.getKvs().stream().findFirst().get().getValue().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get value from etcd", e);
        }
    }
}
