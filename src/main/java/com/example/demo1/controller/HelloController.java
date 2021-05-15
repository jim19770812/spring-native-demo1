package com.example.demo1.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/v1")
public class HelloController {
    @RequestMapping("/hello")//curl http://localhost:8080/v1/hello
    public String index() {
        return "Hello World\n";
    }

    @RequestMapping("/echo/{text}")//curl http://localhost:8080/v1/echo/123123
    public String echo(@PathVariable("text") String text){
        return text+"\n";
    }
}
