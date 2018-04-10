package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.NetworkHashrateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
public class indexController {

    private final NetworkHashrateService netService;

    @RequestMapping("")
    public String getMainPage(Model model){

        model.addAttribute("networkHashrates", netService.getAllNetworks());

        return "main";
    }

    @RequestMapping("/{id}")
    public String getNetworkInfo(Model model, @PathVariable String id){

        model.addAttribute("poolHashrate", netService.getPoolHashrate(new Long(id)));

        return "networkDetails :: modalContents";
    }


}
