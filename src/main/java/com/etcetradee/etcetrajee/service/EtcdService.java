package com.etcetradee.etcetrajee.service;

import com.etcetradee.etcetrajee.utility.KeyStoreConverter;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;


import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;


@Service
public class EtcdService {

    private final Client client;

    public EtcdService() {
        String etcdEndpoint = "http://localhost:2379"; // Change to your etcd endpoint
        this.client = Client.builder().endpoints(etcdEndpoint).build();
    }
    public CompletableFuture<PutResponse> loadKeyStoreInEtcd(String key) {
        try (var kvClient = client.getKVClient()) {
            // Load the KeyStore from a file
            //FileInputStream fis = new FileInputStream(new File("C:\\IntellijProjects\\etcetrajee\\src\\main\\resources\\dtls_keystore.jks"));
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(new File("C:\\IntellijProjects\\etcetrajee\\src\\main\\resources\\dtls_keystore.jks")), "123456".toCharArray());

            // Convert the KeyStore to a ByteSequence
            byte[] keystoreBytes = KeyStoreConverter.toByteArray(keyStore);
            ByteSequence keyStoreByteSequence = ByteSequence.from(keystoreBytes);
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
