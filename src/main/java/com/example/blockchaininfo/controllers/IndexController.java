package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.DisplayDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@Controller
public class IndexController {

    private final DisplayDataService displayDataService;

    @RequestMapping("")
    public String getAllNetworks(Model model){

        model.addAttribute("networkHashrates", displayDataService.getAllNetworks());

        return "main";
    }

    @ResponseBody
    @RequestMapping("/refresh")
    public String refreshPage() throws JsonProcessingException {

        return displayDataService.returnNetworkAsJson();
    }

    @ResponseBody
    @RequestMapping("/pooldata/{id}")
    public String poolsData(@PathVariable String id) throws JsonProcessingException {

        return displayDataService.returnPoolsAsJson(Long.valueOf(id));
    }

    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id) throws JsonProcessingException{

        model.addAttribute("poolHashrates", displayDataService.getAllPools(Long.valueOf(id)));

        return "networkDetails :: modalContents";
    }
}
