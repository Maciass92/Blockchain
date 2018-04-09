package com.example.blockchaininfo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class indexController {

    @RequestMapping("")
    public String getMainPage(){

        return "main";
    }
}
