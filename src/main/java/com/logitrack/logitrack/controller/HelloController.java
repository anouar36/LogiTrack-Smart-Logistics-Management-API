package com.logitrack.logitrack.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @GetMapping("/")
    public String homePage() {
        return "Welcome to home page LogiTrack rr  🚚";
    }
    @GetMapping("/api/hello")
    public String hello() {
        return "Welcome to LogiTrack API 🚚";
    }
}
