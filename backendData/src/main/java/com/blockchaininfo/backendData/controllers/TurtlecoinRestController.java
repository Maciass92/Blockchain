package com.blockchaininfo.backendData.controllers;

import com.blockchaininfo.backendData.services.DisplayDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
public class TurtlecoinRestController {

    private final DisplayDataService displayDataService;

    @RequestMapping("/refresh")
    public String refreshPage() throws JsonProcessingException {

        return displayDataService.returnNetworkAsJson();
    }

    @RequestMapping("/pooldata/{id}")
    public String poolsData(@PathVariable String id) throws JsonProcessingException {

        return displayDataService.returnPoolsAsJson(Long.valueOf(id));
    }


}
