package com.ddbs.choroid_session_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/frontend")
public class FrontendController {

    @GetMapping("/index4")
    public String indexPage() {
        return "index4"; // Spring will resolve this to index4.html in templates
    }

}
