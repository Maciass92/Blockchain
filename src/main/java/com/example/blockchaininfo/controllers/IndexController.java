package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@Controller
public class IndexController {

    private final FindAndDisplayDataService netService;

    @RequestMapping("")
    public String getAllNetworks(Model model){

        model.addAttribute("networkHashrates", netService.getAllNetworks());

        return "main";
    }

    @ResponseBody
    @RequestMapping("/refresh")
    public String refreshPage() throws JsonProcessingException {

        return netService.returnNetworkAsJson();
    }

    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id){

        model.addAttribute("poolHashrates", netService.getAllPools(Long.valueOf(id)));

        return "networkDetails :: modalContents";
    }
}
