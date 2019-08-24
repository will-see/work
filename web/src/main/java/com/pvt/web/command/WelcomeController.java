package com.pvt.web.command;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WelcomeController {
    @RequestMapping(value = {"/","/welcome"})
    public String welcomePage(ModelAndView modelAndView) {
        return "welcome";
    }
}
