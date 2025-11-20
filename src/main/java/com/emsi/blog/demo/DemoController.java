package com.emsi.blog.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo-controller")
public class DemoController {

    @GetMapping
    public ResponseEntity<String> get() {
        return ResponseEntity.ok("The jwt is working!");
    }
}
