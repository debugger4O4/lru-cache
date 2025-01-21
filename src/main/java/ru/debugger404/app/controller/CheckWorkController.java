package ru.debugger404.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Проверка работы сервиса.
 */

@RestController
@RequestMapping("/check")
public class CheckWorkController {

    @GetMapping(value = "/isWork", produces = "text/html")
    public String check() {
        return "<html>" +
                    "<head></head>" +
                    "<body>" +
                        "<h1>" +
                            "Checking the service." +
                        "</h1>" +
                        "<p>" +
                            "The service is working!" +
                        "</p>" +
                    "</body>" +
                "</html>";
    }
}
