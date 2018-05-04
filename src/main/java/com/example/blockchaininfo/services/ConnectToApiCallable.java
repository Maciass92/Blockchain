package com.example.blockchaininfo.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.concurrent.Callable;

@Slf4j
public class ConnectToApiCallable implements Callable<String> {

    private String url;

    public ConnectToApiCallable(String url) {
        this.url = url;
    }

    @Override
    public String call() throws Exception {

            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
            clientHttpRequestFactory.setConnectTimeout(4000);
            clientHttpRequestFactory.setReadTimeout(4000);
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        return restTemplate.getForObject(url, String.class);
    }
}
