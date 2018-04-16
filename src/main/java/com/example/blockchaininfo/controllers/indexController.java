package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.NetworkHashrateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class indexController {

    private final NetworkHashrateService netService;

    @RequestMapping("")
    public String getAllNetworks(Model model){

        model.addAttribute("networkHashrates", netService.getAllNetworks());

        return "main";
    }

    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id){

        model.addAttribute("poolHashrates", netService.getAllPools(new Long(id)));

        return "networkDetails :: modalContents";
    }
}
