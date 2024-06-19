package com.etcetradee.etcetrajee.controller;

import com.etcetradee.etcetrajee.service.EtcdService;
import io.etcd.jetcd.kv.PutResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class EtcdController {

    private final EtcdService etcdService;

    public EtcdController(EtcdService etcdService) {
        this.etcdService = etcdService;
    }

    @GetMapping("/value/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        String value = etcdService.getValueFromEtcd(key);
        return ResponseEntity.ok(value);
    }

    @PutMapping("value/{key}/{value}")
    public ResponseEntity<String> putValue(@PathVariable String key, @PathVariable String value) throws ExecutionException, InterruptedException {
        CompletableFuture<PutResponse> responseCompletableFuture= etcdService.putValueInEtcd(key, value);
        //if(responseCompletableFuture.isDone())
            return ResponseEntity.ok(responseCompletableFuture.get().getPrevKv().getValue().toString());
        //return ResponseEntity.internalServerError().body("Nahi Dali Value");
    }
}

