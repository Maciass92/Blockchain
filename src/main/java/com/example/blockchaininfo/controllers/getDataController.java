package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.GetDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@RestController
public class getDataController {

    private final GetDataService getDataService;

/*    @RequestMapping("/saveNetworkData")
    public void getNetworkData(HttpServletResponse response) throws IOException, URISyntaxException {

        getDataService.storeData();

        response.sendRedirect("");
    }*/
}
