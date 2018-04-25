package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.PageWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@Controller
public class IndexController {

    private final FindAndDisplayDataService netService;

    @RequestMapping("")
    public String getAllNetworks(Model model, Pageable pageable){

        PageWrapper<NetworkHashrate> page = new PageWrapper<NetworkHashrate>(netService.getAllNetworks(pageable), "");

        model.addAttribute("page", page);
        model.addAttribute("networkHashrates", page.getContent());

        return "main";
    }

    @RequestMapping("/refresh")
    public String refreshPage(Model model, Pageable pageable){

        PageWrapper<NetworkHashrate> page = new PageWrapper<NetworkHashrate>(netService.getAllNetworks(pageable), "");

        model.addAttribute("page", page);
        model.addAttribute("networkHashrates", page.getContent());

        return "main :: table-content";
    }

    @RequestMapping("/{id}")
    public String getNetworkDetails(Model model, @PathVariable String id){

        model.addAttribute("poolHashrates", netService.getAllPools(Long.valueOf(id)));

        return "networkDetails :: modalContents";
    }
}
