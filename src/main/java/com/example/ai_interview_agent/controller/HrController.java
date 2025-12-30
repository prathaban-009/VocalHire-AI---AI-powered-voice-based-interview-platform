package com.example.ai_interview_agent.controller;

import com.example.ai_interview_agent.entity.HrRequirement;
import com.example.ai_interview_agent.repository.HrRequirementRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = "*")
public class HrController {
    
    private final HrRequirementRepository repo;
    
    public HrController(HrRequirementRepository repo) {
        this.repo = repo;
    }
    
    @PostMapping("/requirements")
    public HrRequirement create(@RequestBody HrRequirement hr) {
        return repo.save(hr);
    }
    
    @GetMapping("/requirements")
    public List<HrRequirement> getAll() {
        return repo.findAll();
    }
    
    @GetMapping("/requirements/{id}")
    public HrRequirement getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Requirement not found"));
    }
}