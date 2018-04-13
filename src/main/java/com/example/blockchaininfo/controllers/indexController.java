package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.services.NetworkHashrateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @RequestMapping("/savePoolsData")
    public @ResponseBody String getPoolsData(@RequestBody String string){

        System.out.println("Triggered: " + string);
        return "Success mvc";
    }

    @RequestMapping("/saveNetworkData")
    public @ResponseBody String getNetworkData(@RequestBody String string){

        System.out.println("Network triggered: " + string);
        return "Success mvc";
    }
}
