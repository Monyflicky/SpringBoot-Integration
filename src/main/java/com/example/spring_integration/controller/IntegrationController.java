package com.example.spring_integration.controller;

import com.example.spring_integration.model.Address;
import com.example.spring_integration.model.Student;
import com.example.spring_integration.service.IntegrationGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integrate")
public class IntegrationController
{
    @Autowired
    private IntegrationGateway integrationGateway;

    public IntegrationController(IntegrationGateway integrationGateway){
        this.integrationGateway = integrationGateway;
    }

    @GetMapping(value = "{name}")
    public String getMessageFromIntegrationservice(@PathVariable("name") String name){
        return integrationGateway.sendMessage(name);
    }

    @PostMapping("/student")
    public void processStudentDetails(@RequestBody Student student){
        integrationGateway.process(student);
    }

    @PostMapping("/address")
    public void processStudentDetails(@RequestBody Address address){
        integrationGateway.process(address);
    }
}
