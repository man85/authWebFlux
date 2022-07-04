package com.reactive.authWebFlux.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ViewController {

    @GetMapping
    public String home() {
        return "home";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
