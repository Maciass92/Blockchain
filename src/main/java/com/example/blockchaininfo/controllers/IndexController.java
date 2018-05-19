package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.GetDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Controller
public class IndexController {

    private final FindAndDisplayDataService findAndDisplayDataService;

    @RequestMapping("")
    public String getAllNetworks(Model model){

        model.addAttribute("networkHashrates", findAndDisplayDataService.getAllNetworks());

        return "main";
    }

    @ResponseBody
    @RequestMapping("/refresh")
    public String refreshPage() throws JsonProcessingException {

        return findAndDisplayDataService.returnNetworkAsJson();
    }

    @ResponseBody
    @RequestMapping("/pooldata/{id}")
    public String poolsData(@PathVariable String id) throws JsonProcessingException {

        return findAndDisplayDataService.returnPoolsAsJson(Long.valueOf(id));
    }



    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id){

        model.addAttribute("poolHashrates", findAndDisplayDataService.getAllPools(Long.valueOf(id)));

        return "networkDetails :: modalContents";
    }
}
