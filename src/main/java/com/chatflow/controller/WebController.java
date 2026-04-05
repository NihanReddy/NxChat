package com.chatflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA fallback: any route that is not /api/** or a real static file
 * gets forwarded to the React app's index.html.
 */
@CrossOrigin(origins = "*")
@Controller
public class WebController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/signup",
            "/chat",
            "/chat/**"
    })
    public String spa() {
        // Spring Boot auto-serves src/main/resources/static/index.html
        return "forward:/index.html";
    }
}
