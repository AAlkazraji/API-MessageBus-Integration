package com.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ConfigController {

    private final SqsMessageService sqsMessageService;

    @Autowired
    public ConfigController(SqsMessageService sqsMessageService) {
        this.sqsMessageService = sqsMessageService;
    }

    @PostMapping("/config")
    public RedirectView saveConfiguration(@RequestParam int maxQuestionValue, @RequestParam long publishInterval) {
        sqsMessageService.setConfiguration(maxQuestionValue, publishInterval);
        return new RedirectView("index.html");
    }
}
