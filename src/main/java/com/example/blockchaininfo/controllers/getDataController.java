package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.GetDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@RestController
public class getDataController {

    private final GetDataService getDataService;

    @RequestMapping("/savePoolsData")
    public void getPoolsData(HttpServletResponse response) throws IOException, URISyntaxException {

        getDataService.connectToPoolAPIs();

        response.sendRedirect("");
    }

    @RequestMapping("/saveNetworkData")
    public void getNetworkData(HttpServletResponse response) throws IOException {

        getDataService.getNetworkDataAndStoreToDB();

        response.sendRedirect("");
    }
}
