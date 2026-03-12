package com.xawl.cateen.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping
    public Map<String, Object> root() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "OK");
        res.put("message", "Cateen backend is running. Use /api/mini/* and /api/admin/* endpoints.");
        res.put("timestamp", Instant.now().toString());
        return res;
    }

    @GetMapping("health")
    public Map<String, Object> health() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "UP");
        res.put("timestamp", Instant.now().toString());
        return res;
    }
}
