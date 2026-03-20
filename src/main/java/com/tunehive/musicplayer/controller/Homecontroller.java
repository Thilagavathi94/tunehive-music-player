package com.tunehive.musicplayer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Homecontroller {

    // Optional route (no conflict)
    @GetMapping("/home")
    public String home() {
        return "dashboard";
    }
}