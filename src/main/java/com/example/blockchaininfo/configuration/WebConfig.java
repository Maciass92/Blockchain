package com.example.blockchaininfo.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.List;

@Configuration
public class WebConfig{

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers){
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(PageRequest.of(0, 20));
        argumentResolvers.add(resolver);
    }
}
