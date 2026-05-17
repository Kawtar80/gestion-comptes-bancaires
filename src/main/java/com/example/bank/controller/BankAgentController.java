package com.example.bank.controller;

import com.example.bank.service.BankAgentService;
import com.example.bank.service.BankAgentService.AgentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class BankAgentController {

    @Autowired
    private BankAgentService bankAgentService;

    @PostMapping("/command")
    public ResponseEntity<Map<String, Object>> command(@RequestBody Map<String, String> body) {
        String command = body.getOrDefault("command", "");
        AgentResponse response = bankAgentService.processCommand(command);
        return ResponseEntity.ok(Map.of(
                "reply", response.getReply(),
                "success", response.isSuccess()));
    }
}
