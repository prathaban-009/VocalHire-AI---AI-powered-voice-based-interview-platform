package com.example.ai_interview_agent.repository;

import com.example.ai_interview_agent.entity.HrRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrRequirementRepository extends JpaRepository<HrRequirement, Long> {
}