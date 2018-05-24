package com.blockchaininfo.frontend.controllers;

import com.blockchaininfo.backendData.services.DisplayDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
public class TurtlecoinMvcController {

    private final DisplayDataService displayDataService;

    @RequestMapping("")
    public String getAllNetworks(Model model){

        model.addAttribute("networkHashrates", displayDataService.getAllNetworks());

        return "main";
    }

    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id) throws JsonProcessingException{

        model.addAttribute("poolHashrates", displayDataService.getAllPools(Long.valueOf(id)));

        return "networkDetails :: modalContents";
    }
}
