package com.example.blockchaininfo.services;

import com.example.blockchaininfo.pojos.PoolStatus;
import com.example.blockchaininfo.pojos.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.concurrent.Callable;

@Slf4j
public class ConnectToApiCallable implements Callable<PoolStatus> {

    private String url;
    private String name;

    public ConnectToApiCallable(String url, String name) {
        this.url = url;
        this.name = name;
    }

    @Override
    public PoolStatus call() throws Exception {

        PoolStatus poolStatus = new PoolStatus();

        try {
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
            clientHttpRequestFactory.setConnectTimeout(4000);
            clientHttpRequestFactory.setReadTimeout(4000);
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            poolStatus.setJsonResponse(restTemplate.getForObject(url, String.class));
        } catch (ResourceAccessException e){
            log.info("Pool resource access exception: " + e);
        } catch (HttpServerErrorException e2){
            log.info("Pool server error: " + e2);
        } catch (IllegalStateException e3){
            log.info("Pool server error: " + e3);
        }


        /// TODO: 04.05.2018  
        poolStatus.setStatus(Status.BAD_GATEWAY_502_1);


        poolStatus.setName(name);
        poolStatus.setDateTime(OffsetDateTime.now());

        return poolStatus;
    }
}
